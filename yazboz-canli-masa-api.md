# Yazboz Canlı Masa — API Sözleşmesi

Backend ile istemci arasındaki tek doğru kaynak bu dosya. İki taraf da buna uyar. Bir şeyin
değişmesi gerekirse önce bu dosya güncellenir, sonra iki taraf.

## İlkeler

- **Sunucu bir defterdir, hesap makinesi değil.** El ve ceza içeriğini (`payload`) yorumlamaz;
  saklar, sıra numarası verir, dağıtır. Skoru istemci toplar.
- **Tek yazıcı.** Olay ekleme, oyunu bitirme ve yazıcılığı devretme sadece `writerUserId`
  olan kullanıcıya açıktır.
- **Sıra numarası.** Her olay oyuna özel, 1'den başlayan, boşluksuz artan bir `seq` alır.
- **Kod sadece oyun aktifken yaşar.** Oyun bitince kod ölür; bitmiş oyuna katılım yoktur.
- **Oyuncu = koltuk.** Olaylarda oyuncular koltuk numarasıyla (1–4) anılır; isimle veya baş
  harfle değil. Baş harf sadece ekranda gösterim içindir.

## Kimlik

İstemci ilk açılışta rastgele bir `deviceSecret` üretir ve kalıcı saklar.

`POST /auth/device` — gövde: `deviceSecret` → yanıt: `userId`, `accessToken`, `expiresAt`

Aynı secret her zaman aynı kullanıcıyı döndürür. Token süresi dolunca aynı çağrı tekrar
yapılır. Diğer tüm uçlar `Authorization: Bearer <accessToken>` ister.

## Modeller

**Game**

| Alan | Açıklama |
|---|---|
| id | UUID |
| code | 6 karakter (aşağıya bak) |
| joinUrl | `https://<DOMAIN>/j/<code>` |
| status | `ACTIVE` \| `FINISHED` |
| mode | `ESLI` \| `ESSIZ` |
| team1Name, team2Name | eşsizde null |
| writerUserId | şu anki yazıcı |
| writerOnline | yazıcının açık bir canlı bağlantısı var mı |
| writerOfflineSince | yazıcı çevrimdışıysa ne zamandan beri, değilse null |
| lastSeq | son olayın seq'i, olay yoksa 0 |
| createdAt, finishedAt | |

**Seat**

| Alan | Açıklama |
|---|---|
| seatNo | 1–4 |
| name | giriş ekranında yazılan isim |
| team | 1 \| 2, eşsizde null |
| userId | sahiplenen kullanıcı, boşsa null |

**Event**

| Alan | Açıklama |
|---|---|
| seq | sunucu atar |
| clientEventId | istemci üretir (UUID), tekrar gönderimde aynı kalır |
| type | `HAND_ADDED` \| `HAND_EDITED` \| `HAND_DELETED` \| `PENALTY_ADDED` \| `PENALTY_DELETED` |
| payload | JSON obje, sunucu için opak |
| createdBy | userId |
| createdAt | |

**Üyelik:** Bir oyunun üyesi = oyunu oluşturan + koltuk sahiplenenler. `join` dışındaki
okuma uçları üyelik ister.

## Payload şeması (istemciye ait, sunucu doğrulamaz)

Her payload `v: 1` alanı taşır.

| Tip | Alanlar |
|---|---|
| HAND_ADDED | eşlide `teamScores` (2 sayı), eşsizde `seatScores` (4 sayı); `finisherSeat` (koltuk no veya null); `openerSeats` (koltuk no listesi) |
| HAND_EDITED | `targetSeq` + HAND_ADDED'in tüm alanları (tam değiştirme) |
| HAND_DELETED | `targetSeq` |
| PENALTY_ADDED | eşlide `team` zorunlu, `seat` opsiyonel (cezayı kim yedi); eşsizde `seat` zorunlu; `points` (sayı veya null — null ise sadece "X" işareti); `label` (metin veya null) |
| PENALTY_DELETED | `targetSeq` |

## Uçlar

| Metot | Yol | Kim | Ne yapar |
|---|---|---|---|
| POST | `/auth/device` | herkes | yukarıda |
| POST | `/games` | giriş yapmış | Oyun oluşturur. Gövde: `mode`, takım adları, `seats` (seatNo, name, team), `mySeatNo` (opsiyonel), `initialEvents` (clientEventId, type, payload listesi — paylaşmadan önce yerelde girilmiş eller). Oluşturan yazıcıdır. Yanıt: Game + seats + events. |
| POST | `/games/join` | giriş yapmış | Gövde: `code` (büyük/küçük harf duyarsız). Yanıt: Game + seats + events. Kod yoksa veya oyun bitmişse `CODE_NOT_FOUND`. |
| POST | `/games/{id}/seats/{seatNo}/claim` | giriş yapmış, kodla katılmış | Koltuk boşsa ve kullanıcı bu oyunda başka koltukta değilse sahiplenir. |
| GET | `/games/{id}?afterSeq=N` | üye | Game + seats + `seq > N` olaylar |
| POST | `/games/{id}/events` | yazıcı | Gövde: clientEventId, type, payload. Yanıt: `seq`. Aynı clientEventId tekrar gelirse yeni kayıt açılmaz, ilk seq döner. |
| POST | `/games/{id}/writer` | yazıcı | Gövde: `toUserId`. Hedef bu oyunda koltuk sahibi olmalı. Alıcı onayı yok, anında geçer. |
| POST | `/games/{id}/writer/takeover` | koltuk sahibi | Yazıcı çevrimdışıysa ve en az 120 sn geçtiyse çağıranı yazıcı yapar. Aynı anda iki çağrıdan sadece biri başarılı olur. |
| POST | `/games/{id}/finish` | yazıcı | status `FINISHED`, kod serbest kalır. |
| GET | `/games/{id}/stream` | üye | SSE, aşağıya bak |
| GET | `/me/games` | giriş yapmış | Kullanıcının üye olduğu bitmiş oyunlar, yeniden eskiye, sayfalı. Her biri Game + seats + events. |

## Canlı akış (SSE)

- Olay mesajlarında SSE `id` alanı = `seq`.
- İstemci bağlanırken `Last-Event-ID` başlığına son uyguladığı seq'i koyar. Sunucu önce
  güncel `writer` ve tüm `seat` durumlarını, sonra `Last-Event-ID`'den sonraki olayları
  gönderir, ardından canlı akışa geçer. Kaçırılan her şey bağlanınca gelir.
- İstemci `seq ≤ lastSeq` olan olayları yok sayar.

| SSE event adı | Veri |
|---|---|
| `event` | Event |
| `writer` | writerUserId, writerOnline, writerOfflineSince |
| `seat` | Seat |
| `finished` | finishedAt |

Sunucu her 20 sn'de bir yorum satırı (heartbeat) gönderir.

## Kod

- 6 karakter, alfabe: `23456789ABCDEFGHJKMNPQRSTUVWXYZ` (0, O, 1, I, L yok).
- Sadece aktif oyunlar arasında tekil. Oyun bitince aynı kod başka oyuna verilebilir.
- 24 saat hiçbir olay gelmeyen aktif oyun otomatik `FINISHED` olur.

## Hatalar

Gövde: `error` alanında makine kodu.

| HTTP | error |
|---|---|
| 401 | `UNAUTHORIZED` |
| 403 | `NOT_WRITER`, `NOT_MEMBER` |
| 404 | `CODE_NOT_FOUND`, `GAME_NOT_FOUND` |
| 409 | `GAME_FINISHED`, `SEAT_TAKEN`, `ALREADY_SEATED`, `WRITER_STILL_ONLINE`, `TARGET_NOT_SEATED` |

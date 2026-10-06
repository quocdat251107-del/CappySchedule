# Cappy Schedule 📅⚔️

**CappySchedule** là một plugin Minecraft Spigot / Paper cao cấp hỗ trợ lên lịch trình tự động triệu hồi (spawn) Boss / Mob theo múi giờ tùy chỉnh (Timezone) với hệ thống thông báo đếm ngược (Countdown Announcement), hỗ trợ đầy đủ **MythicMobs**, **EliteMobs**, **Vanilla Mobs** và **Custom Commands**.

---

## 🌟 Tính Năng Nổi Bật

- 🕒 **Hỗ trợ Múi giờ Thực tế (Timezone)**: Cấu hình múi giờ bất kỳ (ví dụ: `Asia/Ho_Chi_Minh`, `GMT+7`, `UTC`, `America/New_York`, ...).
- 👾 **Đa dạng nguồn Mob (Providers)**:
  - **MythicMobs**: Triệu hồi MythicMob theo ID và Level.
  - **EliteMobs**: Triệu hồi Custom Boss / Mob cấp độ cao.
  - **Vanilla Mobs**: Triệu hồi mob gốc với tên hiển thị tùy chỉnh (Zombie, Giant, Wither, Ender Dragon, ...).
  - **Commands**: Kích hoạt lệnh console / sự kiện theo lịch trình.
- ⏰ **Lịch trình Linh hoạt**:
  - Khung giờ cố định trong ngày: `["09:00", "14:30", "20:00", "22:30"]`.
  - Chọn các ngày trong tuần: `ALL`, `WEEKEND`, `WEEKDAY`, hoặc từng ngày `[MONDAY, WEDNESDAY, FRIDAY]`.
  - Lặp lại theo chu kỳ (Interval): `interval: "every: 3h"` hoặc `interval: "every: 30m"`.
- 📢 **Hệ thống Cảnh báo & Đếm ngược (Pre-Spawn Warnings)**:
  - Thông báo trước khi boss xuất hiện (ví dụ: 10 phút, 5 phút, 1 phút, 10 giây).
  - Hỗ trợ đầy đủ: **Chat Message**, **Title & Subtitle**, **Action Bar**, và **Hiệu ứng âm thanh (Sound)**.
- 🎯 **Vị trí Triệu hồi Thông minh**:
  - Hỗ trợ đa thế giới (Multi-World).
  - Tự động tìm bề mặt an toàn (`safe_spawn: true`).
  - Bán kính triệu hồi ngẫu nhiên (`radius`).
- 🛡️ **Bỏ qua cấm Spawn của WorldGuard (WorldGuard Bypass)**:
  - `ignore-worldguard: true`: Cho phép triệu hồi Boss ngay cả trong các Region WorldGuard đã bật cờ cấm quái (`mob-spawning: deny`).
- 🛡️ **Chống dồn Boss & Tự biến mất (Despawn Timer)**:
  - `prevent_stacking: true`: Không spawn thêm boss nếu boss trước đó vẫn chưa bị tiêu diệt.
  - `despawn_after_seconds`: Tự động biến mất sau X giây nếu không có ai khiêu chiến.
- 🏆 **Phần thưởng & Vinh danh người hạ gục (Kill Rewards & Broadcast)**:
  - Tự động ghi nhận người chơi tiêu diệt boss và chạy lệnh thưởng (vd: cộng tiền, cấp vật phẩm).
- 🎨 **Hỗ trợ Màu sắc Hex & MiniMessage**: Hỗ trợ mã màu `&` truyền thống và mã màu Hex `&#RRGGBB`.

---

## 📋 Yêu Cầu Hệ Thống

| Thành phần | Yêu cầu |
| :--- | :--- |
| **Minecraft Server** | Spigot / Paper / Purpur **1.20.x đến 1.21.x** (hoặc mới hơn) |
| **Java Runtime** | Java **17** hoặc **Java 21+** |
| **Plugins hỗ trợ (Tùy chọn)** | [WorldGuard](https://enginehub.org/worldguard), [MythicMobs](https://mythiccraft.io/) (v5.x), [EliteMobs](https://www.spigotmc.org/resources/elitemobs.40090/), [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) |

---

## 🛠️ Hướng Dẫn Cài Đặt

1. Tải tệp `CappySchedule-1.0.3.jar` từ thư mục [`build/libs/CappySchedule-1.0.3.jar`](build/libs/CappySchedule-1.0.3.jar).
2. Đặt file jar vào thư mục `plugins/` của máy chủ Minecraft.
3. Khởi động lại máy chủ hoặc nạp plugin qua PlugMan.
4. Chỉnh sửa cấu hình trong `plugins/CappySchedule/config.yml` và `messages.yml`.
5. Sử dụng lệnh `/cs reload` để áp dụng cấu hình mới mà không cần khởi động lại máy chủ.

---

## 🎮 Lệnh & Quyền Hạn (Commands & Permissions)

| Lệnh | Alias | Quyền hạn | Mô tả |
| :--- | :--- | :--- | :--- |
| `/cappyschedule help` | `/cs help` | `cappyschedule.use` | Hiển thị bảng trợ giúp |
| `/cappyschedule next` | `/cs next` | `cappyschedule.player.next` | Xem danh sách boss sắp xuất hiện |
| `/cappyschedule time` | `/cs time` | `cappyschedule.player.time` | Xem giờ máy chủ theo múi giờ cấu hình |
| `/cappyschedule list` | `/cs list` | `cappyschedule.admin.list` | *(Admin)* Xem tất cả lịch trình và trạng thái boss |
| `/cappyschedule trigger <id>` | `/cs trigger <id>` | `cappyschedule.admin.trigger` | *(Admin)* Kích hoạt thủ công 1 lịch trình ngay lập tức |
| `/cappyschedule reload` | `/cs reload` | `cappyschedule.admin.reload` | *(Admin)* Tải lại cấu hình và lịch trình |

---

## ⚙️ Cấu Hình Mẫu (`config.yml`)

```yaml
# Múi giờ hoạt động
timezone: "Asia/Ho_Chi_Minh"

# Định dạng ngày giờ
time-format: "HH:mm:ss"
date-format: "yyyy-MM-dd"
datetime-format: "yyyy-MM-dd HH:mm:ss"

# Tiền tố hiển thị
prefix: "&#FF8C00[&#FFA500CappySchedule&#FF8C00]&r "

schedules:
  # Ví dụ 1: MythicMobs World Boss
  world_boss_skeletor:
    enabled: true
    display_name: "&#FF4500&l[WORLD BOSS] Skeletor King"
    provider: "MYTHICMOBS" # MYTHICMOBS, ELITEMOBS, VANILLA, COMMAND
    mob_id: "SkeletorKing"
    level: 1
    times:
      - "09:00"
      - "14:00"
      - "20:00"
      - "22:30"
    days:
      - "ALL"
    locations:
      - world: "world"
        x: 100.5
        y: 65.0
        z: -250.5
        radius: 5.0
        safe_spawn: true
    prevent_stacking: true
    despawn_after_seconds: 1800 # Tự hủy sau 30 phút
    warnings:
      - time_before: "10m"
        broadcast_chat: true
        chat_message: "{prefix}&eTrùm &f{mob_name} &esẽ xuất hiện sau &c10 phút &etại &b{world} ({x}, {y}, {z})&e!"
        title: "&#FF5555&lCẢNH BÁO"
        subtitle: "&eTrùm &f{mob_name} &exuất hiện sau &c10 phút&e!"
        sound: "BLOCK_NOTE_BLOCK_BELL:1.0:1.0"
      - time_before: "1m"
        broadcast_chat: true
        chat_message: "{prefix}&c&lCẢNH BÁO: &f{mob_name} &csẽ xuất hiện sau &e60 giây&c!"
        sound: "BLOCK_NOTE_BLOCK_PLING:1.0:2.0"
    on_spawn:
      broadcast_chat: true
      chat_message: "{prefix}&c&l[XUẤT HIỆN] &f{mob_name} &cđã xuất hiện tại &e[{world}: {x}, {y}, {z}]&c!"
      sound: "ENTITY_WITHER_SPAWN:1.0:0.8"
      spawn_fireworks: true
    on_kill:
      broadcast_chat: true
      chat_message: "{prefix}&a&l[CHIẾN THẮNG] &f{mob_name} &ađã bị tiêu diệt bởi &e{killer}&a!"
      commands:
        - "eco give {killer} 5000"
```

---

## 🔨 Hướng Dẫn Tự Biên Dịch (Build Project)

Dự án được cấu hình sẵn với **Gradle Wrapper**:

```bash
# Trên Windows
.\gradlew.bat build

# Trên Linux / macOS
./gradlew build
```

Tệp jar sau khi biên dịch sẽ nằm tại:
📁 `build/libs/CappySchedule-1.0.0.jar`

---

## 📄 Bản Quyền & Giấy Phép

Phát triển bởi **CappyTeam**. Dự án được phát hành theo giấy phép [MIT License](LICENSE).

# Real-Time Energy Monitoring and Controlling System

An Android application built with Java that provides real-time energy monitoring and control capabilities using Tuya smart plugs. The app fetches power consumption data via Tuya Cloud API, stores historical data locally in SQLite database, and calculates electricity bills in Bangladeshi Taka (BDT).

## 🌟 Features

- **Real-Time Monitoring**: Monitor voltage, current, and power consumption in real-time from Tuya smart plugs
- **Remote Control**: Control connected appliances remotely through the app
- **Energy Calculation**: Automatically calculates total energy consumption (kWh)
- **Bill Estimation**: Calculates electricity bills in BDT based on consumption
- **Historical Data**: Stores consumption history locally using SQLite database
- **Data Visualization**: View consumption trends and patterns over time
- **User-Friendly Interface**: Clean and intuitive Android interface

## 📱 Screenshots

<!-- Add your app screenshots here -->

## 🔧 Technology Stack

- **Platform**: Android
- **Language**: Java
- **IDE**: Android Studio
- **Smart Plug**: Tuya Smart Plug
- **API**: Tuya Cloud API (via Tuya IoT Platform)
- **Networking**: Retrofit 2
- **Database**: SQLite
- **Cloud Service**: Tuya Developer Platform

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│           Android Application (Java)             │
│                                                   │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────┐│
│  │     UI      │  │   Business   │  │ SQLite  ││
│  │  (Activity/ │◄─┤    Logic     │◄─┤Database ││
│  │  Fragment)  │  │              │  │         ││
│  └─────────────┘  └──────────────┘  └─────────┘│
│         ▲                ▲                       │
│         │                │                       │
│         ▼                ▼                       │
│  ┌─────────────────────────────┐               │
│  │      Retrofit API Client     │               │
│  └─────────────────────────────┘               │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
         ┌──────────────────────┐
         │   Tuya Cloud API     │
         └──────────────────────┘
                   │
                   ▼
         ┌──────────────────────┐
         │   Tuya Smart Plug    │
         └──────────────────────┘
```

## 📋 Prerequisites

Before you begin, ensure you have:

- **Android Studio**: Arctic Fox or later
- **JDK**: Java Development Kit 8 or higher
- **Tuya Developer Account**: Register at [Tuya IoT Platform](https://iot.tuya.com/)
- **Tuya Smart Plug**: Compatible Tuya smart plug device
- **Android Device/Emulator**: Running Android 5.0 (API 21) or higher

### Tuya Developer Setup

1. Create an account on [Tuya IoT Platform](https://iot.tuya.com/)
2. Create a new Cloud Project
3. Link your Tuya Smart Plug device to the project
4. Obtain the following credentials:
   - Access ID (Client ID)
   - Access Secret (Client Secret)
   - Device ID

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/AbrarShakhi/Real-Time-Energy-Monitoring-and-Controlling-System.git
cd Real-Time-Energy-Monitoring-and-Controlling-System
```

### 2. Open in Android Studio

- Launch Android Studio
- Select "Open an Existing Project"
- Navigate to the cloned repository folder
- Click "OK" and wait for Gradle sync

### 3. Configure Tuya API Credentials

Go to [Tuya developer](https://developer.tuya.com/en/) page and create a account. then you need to create a cloud project and add you device.

or you can watch this video on youtube.

![you can watch this video](https://www.youtube.com/watch?v=w-BawMpxBYs&t=79s)


### 4. Add Dependencies

Ensure your `build.gradle (Module: app)` includes:

```gradle
dependencies {
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
```

### 5. Build and Run

- Connect your Android device or start an emulator
- Click "Run" (Shift + F10) in Android Studio
- Select your device and wait for installation

## 📊 Key Features Explained

### Energy Data Collection

The app fetches the following parameters from Tuya smart plug:
- **Voltage (V)**: Real-time voltage measurement
- **Current (A)**: Real-time current consumption
- **Power (W)**: Instantaneous power usage
- **Energy (kWh)**: Calculated cumulative energy consumption

### Bill Calculation

The app calculates electricity bills based on Bangladesh electricity tariff:

```java
private double calculateBill(float energyKWh) {
    double bill;

    if (energyKWh <= 50) {
        bill = energyKWh * 4.63;
    } else if (energyKWh <= 75) {
        bill = 50 * 4.63 + (energyKWh - 50) * 5.26;
    } else if (energyKWh <= 200) {
        bill = 50 * 4.63 + 25 * 5.26 + (energyKWh - 75) * 7.20;
    } else if (energyKWh <= 300) {
        bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + (energyKWh - 200) * 7.59;
    } else if (energyKWh <= 400) {
        bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + 100 * 7.59 + (energyKWh - 300) * 8.02;
    } else if (energyKWh <= 600) {
        bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + 100 * 7.59 + 100 * 8.02 + (energyKWh - 400) * 12.67;
    } else {
        bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + 100 * 7.59 + 100 * 8.02 + 200 * 12.67 + (energyKWh - 600) * 14.61;
    }

    return bill;
}
```

### SQLite Database Schema

```sql
CREATE TABLE energy_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    voltage REAL,
    current REAL,
    power REAL,
    energy REAL,
);
```

## 🔌 Tuya API Integration

### API Endpoints Used

- **Device Status**: Get real-time device data
- **Device Control**: Turn device on/off
- **Device Statistics**: Historical consumption data

### Example Retrofit Interface

```java
public interface TuyaApiService {

    @GET("/v1.0/token")
    Call<TuyaTokenResponse> getToken(
        @HeaderMap Map<String, String> headers,
        @Query("grant_type") int grantType
    );

    @POST("/v1.0/iot-03/devices/{device_id}/commands")
    Call<TuyaCommandResponse> sendCommand(
        @HeaderMap Map<String, String> headers,
        @Path("device_id") String deviceId,
        @Body TuyaCommand body
    );

    @GET("/v2.0/cloud/thing/{device_id}/shadow/properties")
    Call<TuyaShadowPropertiesResponse> getShadowProperties(
        @HeaderMap Map<String, String> headers,
        @Path("device_id") String deviceId
    );

    @GET("/v1.0/token/{refresh_token}")
    Call<TuyaTokenResponse> getNewToken(
        @HeaderMap Map<String, String> headers,
        @Path("refresh_token") String refreshToken
    );
}
```

## 📱 App Structure

```
├── api
│   ├── RetrofitInstance.java
│   ├── TuyaApiService.java
│   └── TuyaOpenApi.java
├── data
│   ├── DeviceInfoDb.java
│   └── PowerConsumptionHistDb.java
├── DeviceDetailActivity.java
├── DeviceInfoActivity.java
├── DeviceService.java
├── MainActivity.java
├── model
│   ├── DeviceInfo.java
│   ├── StatRecord.java
│   ├── TuyaCommand.java
│   ├── TuyaCommandResponse.java
│   ├── TuyaDeviceToken.java
│   ├── TuyaShadowPropertiesResponse.java
│   ├── TuyaTokenInfo.java
│   └── TuyaTokenResponse.java
└── utils
    ├── DeviceListAdapter.java
    └── TuyaSign.java
```

## 🎯 Usage

### First Time Setup

1. Open the app and grant necessary permissions
2. Configure Tuya credentials in Settings
3. Connect to your Tuya smart plug
4. Start monitoring

### Daily Usage

1. **Monitor Tab**: View real-time energy consumption
2. **Control Tab**: Turn devices on/off remotely
3. **History Tab**: View consumption history and trends
4. **Bills Tab**: Check estimated electricity bills

## 🔒 Permissions Required

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

```

## 🐛 Troubleshooting

### API Connection Issues
- Verify Tuya credentials are correct
- Check internet connectivity
- Ensure device is online in Tuya app
- Verify API region endpoint

### Data Not Updating
- Check if smart plug is powered on
- Verify device ID is correct
- Review API rate limits

### Database Issues
- Clear app cache if data corruption occurs
- Check storage permissions
- Verify SQLite database integrity

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards

- Follow Java coding conventions
- Comment complex logic
- Write meaningful commit messages
- Test on multiple Android versions

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Abrar Shakhi**
- GitHub: [@AbrarShakhi](https://github.com/AbrarShakhi)
<!-- - Email: [] -->

## 🙏 Acknowledgments

- [Tuya IoT Platform](https://iot.tuya.com/) for smart plug integration
- [Retrofit](https://square.github.io/retrofit/) for API communication
- Android community for various libraries and resources

## 📚 Resources

- [Tuya API Documentation](https://developer.tuya.com/en/docs/cloud/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Android SQLite Guide](https://developer.android.com/training/data-storage/sqlite)
- [Bangladesh Electricity Tariff](https://dpdc.gov.bd/tariff-rate/)

## 🔮 Future Enhancements

- [ ] Multi-device support
- [ ] Export data to CSV/Excel
- [ ] Push notifications for high consumption
- [ ] Widget for home screen
- [ ] Dark mode support
- [ ] Cloud data backup
- [ ] Consumption predictions using ML
- [ ] Integration with other smart home platforms
- [ ] Comparison with previous months
- [ ] Budget alerts and limits

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
<!-- - Email: [] -->

---

⭐ If you find this project useful, please consider giving it a star!
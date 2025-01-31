//UserData.java
package org.example;

import java.time.LocalTime;

public class UserData {
    private long chatId;
    private String city;
    private int notificationTime; // 0 - нет рассылки, 1 - утром, 2 - днем, 3 - вечером, 4 - весь день

    public UserData(long chatId, String city, int notificationTime) {
        this.chatId = chatId;
        this.city = city;
        this.notificationTime = notificationTime;
    }

    // Геттеры и сеттеры
    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(int notificationTime) {
        this.notificationTime = notificationTime;
    }
}
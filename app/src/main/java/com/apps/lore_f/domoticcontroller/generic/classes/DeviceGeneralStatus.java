package com.apps.lore_f.domoticcontroller.generic.classes;

public class DeviceGeneralStatus extends DeviceStatus {

    public DeviceGeneralStatus(String jsonSource) {
        super(jsonSource);
    }

    public long getRunningSince() {
        return getLongValue("RunningSince");
    }

    public float getTotalSpace() {
        return getFloatValue("TotalSpace");
    }

    public float getFreeSpace() {
        return getLongValue("FreeSpace");
    }

    private String[] getUptime() {
        return getStringValue("Uptime").split("load average: ");
    }

    public int getConnectedUsersCount() {

        String[] data = getUptime();

        if (data.length == 2) {

            try {
                return Integer.parseInt(data[0].split(",")[2].trim().split(" ")[0]);
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                return -1;
            }

        } else {
            return -1;
        }

    }

    public int getAverageLoad() {

        String[] data = getUptime();

        if (data.length == 2) {

            try {
                return (int) (Double.parseDouble(data[1].split(", ")[0].replaceAll(",", ".")) * 100.0);
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                return -1;
            }

        } else {
            return -1;
        }
    }

}

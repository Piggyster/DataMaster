package me.piggyster.datamaster.api.util;

import org.bukkit.configuration.ConfigurationSection;

public class DataMasterSettings {

    // TODO implement more checks, support for other database types and a more solid settings section

    public static DataMasterSettings loadFromSection(ConfigurationSection section) {
        DataMasterSettings settings = new DataMasterSettings();

        settings.setDatabaseType(section.getString("type"));
        settings.setAddress(section.getString("address"));
        settings.setDatabaseName(section.getString("database"));

        return settings;
    }

    private String databaseType;
    private String address;
    private String databaseName;

    public DataMasterSettings() {
        databaseType = null;
        address = null;
        databaseName = null;
    }

    public DataMasterSettings setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public DataMasterSettings setAddress(String address) {
        this.address = address;
        return this;
    }

    public DataMasterSettings setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getAddress() {
        return address;
    }

    public String getDatabaseName() {
        return databaseName;
    }


}

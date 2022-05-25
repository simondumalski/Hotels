package me.simondumalski.hotels.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class Hotel {

    private int timeLeft;
    private Chunk chunk;
    private Player hotelOwner;

    public Hotel(int timeLeft, Chunk chunk, Player hotelOwner) {
        this.timeLeft = timeLeft;
        this.chunk = chunk;
        this.hotelOwner = hotelOwner;
    }

    /**
     * Returns the time left in a hotel booking in seconds
     * @return Time left in seconds
     */
    public int getTimeLeft() {
        return timeLeft;
    }

    /**
     * Sets the time left in a hotel booking to the provided value
     * @param timeLeft New time left to set in seconds
     */
    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    /**
     * Adds more time to a hotel booking
     * @param timeLeft Time to add in days
     */
    public void addTimeLeft(int timeLeft) {
        this.timeLeft += (timeLeft * 60 * 60 * 24);
    }

    /**
     * Decrements the time left in a hotel booking by 1
     */
    public void decrementTime() {
        this.timeLeft--;
    }

    /**
     * Returns the chunk that the hotel is located in
     * @return Chunk the hotel is in
     */
    public Chunk getChunk() {
        return chunk;
    }

    /**
     * Returns the player that owns the hotel booking
     * @return Player that owns the hotel booking
     */
    public Player getHotelOwner() { return hotelOwner; }

    /**
     * Teleports a player to the center of the hotel's chunk
     * @param p Player to teleport to the hotel
     */
    public void teleportToHotel(Player p) {

        //Get the center of the hotel's chunk
        Location chunkCenter = (new Location(
                chunk.getWorld(),
                (chunk.getX() << 4),
                0.0D,
                (chunk.getZ() << 4))
        ).add(-7.0D, 0.0D, -7.0D);
        chunkCenter.setY(chunkCenter.getY() + 5.0D);

        //Teleport the player to the center of the hotel's chunk
        p.teleportAsync(chunkCenter);

    }

    /**
     * Log a hotel creation to the log file
     * @param logFile File to log the entry to
     */
    public void logHotelCreation(File logFile) {

        //Try to append the log entry to the log file
        try (FileWriter writer = new FileWriter(logFile, true)) {

            writer.write(
                    "Hotel Creation: " + hotelOwner.getName()
                            + " Date: " + Date.from(Instant.now())
                            + " Period: " + timeToString()
                            + System.lineSeparator()
            );

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Log a hotel renewal to the log file
     * @param logFile File to log the entry to
     */
    public void logHotelRenewal(File logFile) {

        //Try to append the log entry to the log file
        try (FileWriter writer = new FileWriter(logFile, true)) {

            writer.write(
                    "Hotel Renewal: " + hotelOwner.getName()
                            + " Date: " + Date.from(Instant.now())
                            + " Period: " + timeToString()
                            + System.lineSeparator()
            );

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Log a hotel removal to the log file
     * @param logFile File to log the entry to
     */
    public void logHotelRemoval(File logFile) {

        //Try to append the hotel removal to the log file
        try (FileWriter writer = new FileWriter(logFile, true)) {

            writer.write(
                    "Hotel Removal: " + hotelOwner.getName()
                            + " Date: " + Date.from(Instant.now())
                            + System.lineSeparator()
            );

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Get the time left in a hotel booking in a pleasing format
     * @return Time left in days, hours, minutes, and seconds
     */
    public String timeToString() {

        int seconds = this.timeLeft;
        if (seconds < 60) {
            return seconds + "s";
        }

        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (minutes < 60) {
            return ((minutes < 10) ? "0" : "") + minutes + "m "
                    + ((remainingSeconds < 10) ? "0" : "") + remainingSeconds + "s";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours < 24) {
            return ((hours < 10) ? "0" : "") + hours + "h "
                    + ((remainingMinutes < 10) ? "0" : "") + remainingMinutes + "m "
                    + ((remainingSeconds < 10) ? "0" : "") + remainingSeconds + "s";
        }
        int days = hours / 24;
        int remainingHours = hours % 24;
        return ((days < 10) ? "0" : "") + days + "d "
                + ((remainingHours < 10) ? "0" : "") + remainingHours + "h "
                + ((remainingMinutes < 10) ? "0" : "") + remainingMinutes + "m "
                + ((remainingSeconds < 10) ? "0" : "") + remainingSeconds + "s";

    }

    @Override
    public String toString() {
        return "Hotel{" +
                "timeLeft=" + timeLeft +
                ", chunkX=" + chunk.getX() +
                ", chunkZ=" + chunk.getZ() +
                ", hotelOwner=" + hotelOwner.getName() +
                ", hotelOwnerUUID=" + hotelOwner.getUniqueId() +
                '}';
    }
}

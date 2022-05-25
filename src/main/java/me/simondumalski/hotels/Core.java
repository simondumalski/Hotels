package me.simondumalski.hotels;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.simondumalski.hotels.commands.admin.AdminCommandManager;
import me.simondumalski.hotels.commands.player.CommandManager;
import me.simondumalski.hotels.listeners.BlockBreak;
import me.simondumalski.hotels.listeners.BlockPlace;
import me.simondumalski.hotels.listeners.PlayerInteract;
import me.simondumalski.hotels.listeners.PlayerJoin;
import me.simondumalski.hotels.utils.Hotel;
import me.simondumalski.hotels.utils.HotelWorldGenerator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public final class Core extends JavaPlugin {

    private Economy economy;
    private World hotelsWorld;
    private Map<UUID, Hotel> hotelsMap;
    private final File LOG_FILE = new File(getDataFolder() + File.separator + "log.txt");

    @Override
    public void onEnable() {

        //Save the default config.yml if it doesn't exist
        saveDefaultConfig();
        saveResource("hotels.yml", false);
        saveResource("HotelRoom.schem", false);
        saveResource("log.txt", false);

        //Load the hotels world
        this.hotelsWorld = getServer().getWorld("Hotels");

        //Check if the hotels world exists and create it if it doesn't
        if (hotelsWorld == null) {
            getServer().createWorld(new HotelWorldGenerator("Hotels"));
            hotelsWorld = getServer().getWorld("Hotels");
        }

        //Set the gamerules, difficulty, time, and weather for the hotels world
        hotelsWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        hotelsWorld.setGameRule(GameRule.MOB_GRIEFING, false);
        hotelsWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        hotelsWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        hotelsWorld.setDifficulty(Difficulty.PEACEFUL);
        hotelsWorld.setTime(6000L);
        hotelsWorld.setThundering(false);

        //Register the economy provider
        this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

        //Initialize the hotels hash map
        this.hotelsMap = Maps.newHashMap();

        //Register the event listeners
        getServer().getPluginManager().registerEvents(new BlockBreak(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlace(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);

        //Register the commands
        getCommand("hotels").setExecutor(new CommandManager(this));
        getCommand("hotelsadmin").setExecutor(new AdminCommandManager(this));

        //Load the hotels from the hotels.yml data file
        loadHotels();

        //Schedule the decrement time and remove expired hotels task
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            //Loop through the hotels map
            for (Hotel hotel : hotelsMap.values()) {

                //Decrement the time left in a booking
                hotel.decrementTime();

                //Check if the time left is less than 1 and remove the hotel/log the removal if it is
                if (hotel.getTimeLeft() < 1) {
                    hotel.logHotelRemoval(LOG_FILE);
                    hotelsMap.remove(hotel.getHotelOwner().getUniqueId());
                }

            }

        }, 20L, 20L);

        //Schedule the task for messaging players about their hotel booking time left
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            //Get all the online players and loop through them
            getServer().getOnlinePlayers().forEach((p) -> {

                //Check if the player is renting a hotel
                if (hotelsMap.containsKey(p.getUniqueId())) {

                    //Get the hotel the player is renting
                    Hotel hotel = hotelsMap.get(p.getUniqueId());

                    //Check if the hotel's time left is less than 1 day and send the owner a message if it is
                    if (hotel.getTimeLeft() < 86400) {
                        sendPlayerMessage(p, "messages.events.rent-expiry-alert", new String[]{hotel.timeToString()});
                    }

                }

            });

        }, 20L, (20L * getConfig().getInt("hotels.expiry-alert-interval")));

    }

    @Override
    public void onDisable() {

        //Cancel the scheduled tasks
        getServer().getScheduler().cancelTasks(this);

        //Setup the YamlConfiguration object
        YamlConfiguration config = new YamlConfiguration();

        try {

            config.load(new File(getDataFolder() + File.separator + "hotels.yml"));

            //Clear the data in the file
            config.set("data", null);

            //Loop through the hotels map, adding each hotel to the data file
            for (Hotel hotel : hotelsMap.values()) {

                ConfigurationSection data;

                //Check if the data section exists, creating it if it doesn't
                if (!config.isConfigurationSection("data")) {
                    data = config.createSection("data");
                } else {
                    data = config.getConfigurationSection("data");
                }

                //Save the hotel to the data file
                UUID uuid = hotel.getHotelOwner().getUniqueId();
                data.set(uuid + ".hotel.timeleft", hotel.getTimeLeft());
                data.set(uuid + ".hotel.chunk.x", hotel.getChunk().getX());
                data.set(uuid + ".hotel.chunk.z", hotel.getChunk().getZ());


            }

            config.save(new File(getDataFolder() + File.separator + "hotels.yml"));

        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();

            //Dump the hotel data to console
            for (Hotel hotel : hotelsMap.values()) {
                System.out.println(hotel);
            }

            System.out.println(ChatColor.RED + "Error savings hotels to file! Data may have been lost!");
            System.out.println(ChatColor.RED + "Hotels hashmap has been dumped to console.");
        }

    }

    /**
     * Returns the economy service provider for the plugin
     * @return Economy service provider
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Returns the world used for storing hotels
     * @return World that the hotels are in
     */
    public World getHotelsWorld() {
        return hotelsWorld;
    }

    /**
     * Returns the hashmap used for storing hotel data
     * @return Hashmap that the hotel data is stored in
     */
    public Map<UUID, Hotel> getHotelsMap() {
        return hotelsMap;
    }

    /**
     * Returns the file used for logging hotel tasks
     * @return The file used for logging
     */
    public File getLogfile() {
        return LOG_FILE;
    }

    /**
     * Get the amount of time a player is allowed to rent a hotel for
     * @param p Player to get allowed rental period for
     * @return Allowed rental period for the provided player
     */
    public int getAllowedRentalPeriod(Player p) {
        if (p.hasPermission("hotels.level2"))
            return 28;
        if (p.hasPermission("hotels.rent"))
            return 14;
        else
            return 0;
    }

    /**
     * Send the specified player a message that has been defined in the config.yml
     * @param p Player to send the message to
     * @param configValue Message from the config you want to send
     * @param args The arguments for the argument placeholders. Can be null
     */
    public void sendPlayerMessage(Player p, String configValue, String[] args) {

        //Get the message from the config
        String message = getConfig().getString(configValue);

        //Replace the prefix placeholder if the message contains it
        if (message.contains("%prefix%")) {
            message = message.replace("%prefix%", getConfig().getString("messages.plugin.prefix"));
        }

        //Check if there are message arguments and replace the placeholders if there are
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                message = message.replace("%args" + Integer.toString(i) + "%", args[i]);
            }
        }

        //Send the player the message
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

    }

    /**
     * Send the specified player the header that has been defined in the config.yml
     * @param p Player to send the message to
     */
    public void sendMessageHeader(Player p) {

        //Get the header from the config
        String header = getConfig().getString("messages.plugin.header");

        //Replace the version placeholder if the message contains it
        if (header.contains("%version%")) {
            header = header.replace("%version%", getDescription().getVersion());
        }

        //Send the header to the player
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

    }

    /**
     * Send the secified player the footer that has been defined in the config.yml
     * @param p Player to send the message to
     */
    public void sendMessageFooter(Player p) {

        //Get the footer from the config
        String footer = getConfig().getString("messages.plugin.footer");

        //Replace the version placeholder if the message contains it
        if (footer.contains("%version%")) {
            footer = footer.replace("%version%", getDescription().getVersion());
        }

        //Send the footer to the player
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', footer));

    }

    /**
     * Load the hotels being rented from the hotels.yml data file
     */
    public void loadHotels() {

        //Setup the YamlConfiguration object
        YamlConfiguration config = new YamlConfiguration();

        try {

            //Get the hotels.yml config file
            config.load(new File(getDataFolder() + File.separator + "hotels.yml"));

            //Check if the data section exists and read the data from it it does
            if (config.isConfigurationSection("data")) {
                for (String id : config.getConfigurationSection("data").getKeys(false)) {

                    //Get the UUID
                    UUID uuid = UUID.fromString(id);

                    //Get the section of the file that matches the UUID
                    ConfigurationSection data = config.getConfigurationSection("data").getConfigurationSection(uuid.toString());

                    //Get the attributes for the hotel
                    int timeLeft = data.getInt("hotel.timeleft");
                    int chunkX = data.getInt("hotel.chunk.x");
                    int chunkZ = data.getInt("hotel.chunk.Z");
                    Player hotelOwner = getServer().getPlayer(uuid);

                    //Create the hotel object and add it to the map
                    Hotel hotel = new Hotel(timeLeft, hotelsWorld.getChunkAt(chunkX, chunkZ), hotelOwner);
                    hotelsMap.put(uuid, hotel);

                }
            }

        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Pastes the hotel schematic at the specified chunk
     * @param chunk Chunk the hotel schematic should be pasted
     */
    public synchronized void pasteHotelSchematic(Chunk chunk) {

        //Get the hotel schematic
        File schematic = new File(getDataFolder().getAbsolutePath() + File.separator + "/HotelRoom.schem");

        //Setup the clipboard format
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);

        try {

            //Setup the clipboard reader
            ClipboardReader reader = format.getReader(new FileInputStream(schematic));

            //Load the schematic to the clipboard
            Clipboard clipboard = reader.read();

            //Create the edit session for pasting the schematic
            EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(hotelsWorld)).build();

            //Paste the hotel at the chunk
            Operation operation = (new ClipboardHolder(clipboard))
                    .createPaste((Extent) editSession)
                    .to(BlockVector3.at(
                            chunk.getX() * 16,
                            hotelsWorld.getHighestBlockYAt(
                                    chunk.getX() * 16,
                                    chunk.getZ() * 16
                            ) + 1,
                            chunk.getZ() * 16)
                    )
                    .ignoreAirBlocks(false)
                    .build();

            //Complete the paste operation
            Operations.complete(operation);

            //Close the edit session and clipboard reader
            editSession.close();
            reader.close();

        } catch (IOException | WorldEditException ex) {
            ex.printStackTrace();
            System.out.println(ChatColor.RED + "Error pasting hotel!");
        }

    }

    /**
     * Returns the next available chunk for renting in the hotels world
     * @return Next chunk available for renting
     */
    public synchronized Chunk getNextAvailableChunk() {

        for (int x = -1000; x < 1000; x += 32) {

            for (int z = -320; z < 320; z += 32) {

                int chunkX = x / 16;
                int chunkZ = z / 16;

                Chunk chunk = hotelsWorld.getChunkAt(chunkX, chunkZ);

                //Check if the chunk is loaded and load it if it isn't
                if (!chunk.isLoaded()) {
                    chunk.load(true);
                    chunk.setForceLoaded(true);
                }

                //Check if the map of hotels is empty, returning the chunk if it is
                if (hotelsMap.isEmpty()) {
                    return chunk;
                }

                int free = 0;

                //Loop through the map of hotels
                for (Hotel hotel : hotelsMap.values()) {
                    if (hotel.getChunk().getX() != chunkX || hotel.getChunk().getZ() != chunkZ) {
                        free++;
                    }
                }

                if (free >= hotelsMap.size()) {
                    return chunk;
                }

            }

        }

        throw new IllegalStateException("Unable to find an available chunk in world: " + hotelsWorld.getName());

    }

}

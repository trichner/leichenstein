package ch.n1b.leichenstein;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Created on 16.01.2015.
 *
 * @author Thomas
 */
public class Totengraeber implements Listener {

    private static final int SIGN_LINES = 4;

    private Properties deathCauses;

    private Random rand = new Random();

    public Totengraeber(Properties deathCauses) {
        this.deathCauses = deathCauses;
    }

    private static SimpleDateFormat dformat = new SimpleDateFormat("dd. MMM. yyyy");

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();

        if(e instanceof Player){
            Player player = (Player) e;
            String color = ChatColor.BOLD.toString();
            if(player.getDisplayName().startsWith("n1b")){
                color += ChatColor.DARK_RED.toString();
            }else if(player.getDisplayName().startsWith("xzosimusx")){
                color += ChatColor.GREEN.toString();
            }else if(player.getDisplayName().startsWith("rePeted")){
                color += ChatColor.LIGHT_PURPLE.toString();
            }else if(player.getDisplayName().startsWith("ronfkingswanson")){
                color += ChatColor.DARK_PURPLE.toString();
            }else if(player.getDisplayName().startsWith("AthmosPrime")){
                color += ChatColor.YELLOW.toString();
            }
            String line1 = color + player.getDisplayName();

            String line2 = "[RIP] " + dformat.format(new Date());
            String line3 = "cause of death:";
            String line4 = "";
            Player killer = player.getKiller();
            if(killer!=null){
                line4 = killer.getDisplayName();
            }else {
                EntityDamageEvent devent = player.getLastDamageCause();
                if(devent!=null){
                    DamageCause cause = devent.getCause();
                    line4 = deathCauseToString(cause);
                }
            }
            setGravestone(player.getLocation(),line1,line2,line3,line4);
        }
    }

    private String deathCauseToString(DamageCause cause){
        String mapped = deathCauses.getProperty(cause.name());
        if(mapped==null){
            mapped = cause.name();
        }
        return mapped;
    }

    private void setGravestone(Location location,String... lines){
        // go up until we reach air
        Block block = location.getBlock();
        while (block.getType()!=Material.AIR){
            block = block.getRelative(BlockFace.UP);
        }

        // go down until we reach solid ground or reach the void
        while ((block.isLiquid() || block.getType()==Material.AIR) && (block.getY()>0)){
            block = block.getRelative(BlockFace.DOWN);
        }

        block = block.getRelative(BlockFace.UP);
        block.setType(Material.SIGN_POST);
        block.setData((byte) (rand.nextInt() & 0x0F));
        BlockState state = block.getState();

        if(state instanceof Sign){
            Sign sign = (Sign) state;
            for (int i = 0; i < Math.min(lines.length,SIGN_LINES); i++) {
                sign.setLine(i,lines[i]);
            }

            sign.update(true);
        }else {
            Bukkit.getLogger().warning("Could not place deathsign.");
        }

    }
}

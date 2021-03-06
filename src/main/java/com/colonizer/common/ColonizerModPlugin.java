package com.colonizer.common;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.colonizer.dialog.DefaultingInteractionDialogPlugin;
import com.colonizer.stations.BuildableStationGenerator;
import com.colonizer.stations.dialog.ColonizationInteractionDialogListener;

public class ColonizerModPlugin extends BaseModPlugin {

    public static String VERSION = "1.0.0";

    public static float[] USE_RATE = { 0.1464f, 0.1357f, 0.1250f, 0.1143f, 0.1036f, 0.0929f, 0.0821f, 0.0714f, 0.0607f, 0.0500f };
    public static int[] MAX_IND = { 1, 1, 1, 2, 3, 4, 5, 6, 7, 8 };

    public static Logger logger = Global.getLogger(ColonizerModPlugin.class);

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        CoreImmigrationPluginImpl.INCENTIVE_USE_RATE = (float[]) USE_RATE.clone();
        PopulationAndInfrastructure.MAX_IND = (int[]) MAX_IND.clone();
        SectorThemeGenerator.generators.add(new BuildableStationGenerator());
        DefaultingInteractionDialogPlugin.register(ColonizationInteractionDialogListener.class);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        MemoryAPI memory = Global.getSector().getMemory();
        if (newGame) {
            memory.set("$better_colony_mod", true);
            memory.set("$better_colony_mod.version", VERSION);
            Global.getSector().registerPlugin(new ColonizerCampaignPlugin());
            memory.set("$better_colony_mod.registrations", "com.github.bettercolony.BuildingCampaignPlugin");
        } else {
            Object genObj = memory.get("$better_colony_mod");
            Object verObj = memory.get("$better_colony_mod.version");
            Object regObj = memory.get("$better_colony_mod.registrations");
            boolean already_generated = (genObj != null) ? (boolean) genObj : false;
            String generated_version = (verObj != null) ? (String) verObj : null;
            List<String> registrations = (regObj != null) ?
                Arrays.asList(((String) regObj).split(",")) : new ArrayList<String>();

            // Versioning
            if (!VERSION.equals(generated_version)) {
                // TODO: Implement support for different save versions
                memory.set("$better_colony_mod.version", VERSION);
            }

            // Plugin registration
            if (!registrations.contains("com.github.bettercolony.BuildingCampaignPlugin")) {
                Global.getSector().registerPlugin(new ColonizerCampaignPlugin());
                registrations.add("com.github.bettercolony.BuildingCampaignPlugin");
            }
            if (registrations.size() > 0) {
                memory.set("$better_colony_mod.registrations", Utils.strJoin(",", registrations));
            }

            // Re-generation
            if (!already_generated) {
                // Prepare context
                ThemeGenContext context = new ThemeGenContext();
                Set<Constellation> c = new HashSet<Constellation>();
                for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                    if (system.getConstellation() == null) continue;
                    for (StarSystemAPI curr : system.getConstellation().getSystems()) {
                        if (curr.isProcgen()) {
                            c.add(system.getConstellation());
                            break;
                        }
                    }
                }
                context.constellations = new ArrayList<Constellation>(c);

                // generate
                BuildableStationGenerator generator = new BuildableStationGenerator();
                generator.generateForSector(context, 0f);

                // set already generated
                memory.set("$better_colony_mod", true);
                memory.set("$better_colony_mod.version", VERSION);
            }
        }
    }

}
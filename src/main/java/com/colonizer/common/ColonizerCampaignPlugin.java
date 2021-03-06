package com.colonizer.common;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.colonizer.config.Stations;
import com.colonizer.dialog.DefaultingInteractionDialogPlugin;
import com.colonizer.stations.dialog.*;

import org.apache.log4j.Logger;

public class ColonizerCampaignPlugin extends BaseCampaignPlugin {

	public static Logger logger = Global.getLogger(ColonizerCampaignPlugin.class);

    public String getId() {
		return "MyCampaignPlugin_unique_id"; // make sure to change this for your mod
	}
	
	public boolean isTransient() {
		return false;
	}

	public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
		if (interactionTarget.hasTag(Stations.MINING.locationType)) {
			return new PluginPick<InteractionDialogPlugin>(
                new MiningBuildingDialogPlugin(),
                PickPriority.MOD_GENERAL);
		} else if (interactionTarget.hasTag(Stations.COMMERCIAL.locationType)) {
			return new PluginPick<InteractionDialogPlugin>(
                new CommercialBuildingDialogPlugin(), // TODO: New plugin
                PickPriority.MOD_GENERAL);
		} else {
			if (interactionTarget.hasTag(Tags.OBJECTIVE) || interactionTarget.getMarket() != null)
				return new PluginPick<InteractionDialogPlugin>(new DefaultingInteractionDialogPlugin(), PickPriority.CORE_SET);
			if (interactionTarget.hasTag(Tags.GATE))
				return new PluginPick<InteractionDialogPlugin>(new DefaultingInteractionDialogPlugin(), PickPriority.CORE_SET);
			if (interactionTarget.hasTag(Tags.STATION))
				return new PluginPick<InteractionDialogPlugin>(new DefaultingInteractionDialogPlugin(), PickPriority.CORE_SET);
			if (interactionTarget.hasTag(Tags.HAS_INTERACTION_DIALOG))
				return new PluginPick<InteractionDialogPlugin>(new DefaultingInteractionDialogPlugin(), PickPriority.CORE_SET);
		}
		return null;
    }

}
package twilightforest.data;

import java.util.List;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFParticleType;

import java.util.Iterator;

public class ParticleGenerator extends ParticleDescriptionProvider {

	public ParticleGenerator(PackOutput output, ExistingFileHelper helper) {
		super(output, helper);
	}

	@Override
	protected void addDescriptions() {
		this.sprite(TFParticleType.ANNIHILATE.get(), TwilightForestMod.prefix("annihilate_particle"));
		this.spriteSet(TFParticleType.CLOUD_PUFF.get(), ResourceLocation.withDefaultNamespace("generic"), 8, true);
		this.sprite(TFParticleType.DIM_FLAME.get(), TwilightForestMod.prefix("dim_flame"));
		this.spriteSet(TFParticleType.EXTENDED_SNOW_WARNING.get(), TwilightForestMod.prefix("snow"), 4, false);
		this.sprite(TFParticleType.FALLEN_LEAF.get(), TwilightForestMod.prefix("fallen_leaf"));
		this.sprite(TFParticleType.FIREFLY.get(), TwilightForestMod.prefix("firefly"));
		this.spriteSet(TFParticleType.GHAST_TRAP.get(), ResourceLocation.withDefaultNamespace("generic"), 8, true);
		this.spriteSet(TFParticleType.HUGE_SMOKE.get(), ResourceLocation.withDefaultNamespace("generic"), 8, true);
		this.spriteSet(TFParticleType.ICE_BEAM.get(), TwilightForestMod.prefix("snow"), 4, false);
		this.sprite(TFParticleType.LARGE_FLAME.get(), ResourceLocation.withDefaultNamespace("flame"));
		this.spriteSet(TFParticleType.LEAF_RUNE.get(), () -> new Iterator<>() {
			private int counter = 0;

			@Override
			public boolean hasNext() {
				return this.counter < 26;
			}

			@Override
			public ResourceLocation next() {
				ResourceLocation texture = ResourceLocation.withDefaultNamespace("sga_" + Character.toString('a' + this.counter));
				this.counter++;
				return texture;
			}
		});
		this.sprite(TFParticleType.LOG_CORE_PARTICLE.get(), TwilightForestMod.prefix("log_core"));
		this.sprite(TFParticleType.OMINOUS_FLAME.get(), TwilightForestMod.prefix("ominous_flame"));
		this.sprite(TFParticleType.PARTICLE_SPAWNER_FIREFLY.get(), TwilightForestMod.prefix("firefly"));
		this.spriteSet(TFParticleType.PERFECT_DODGE.get(), ResourceLocation.withDefaultNamespace("generic"), 8, true);
		this.spriteSet(TFParticleType.DOUBLE_JUMP.get(), ResourceLocation.withDefaultNamespace("generic"), 8, true);
		this.spriteSet(TFParticleType.TWILIGHT_PORTAL.get(), ResourceLocation.withDefaultNamespace("generic"), 8, false);
		this.sprite(TFParticleType.PROTECTION.get(), ResourceLocation.withDefaultNamespace("glint"));
		this.spriteSet(TFParticleType.SNOW.get(), TwilightForestMod.prefix("snow"), 4, false);
		// Feywild (MIT) leaves and sparkle of the Ashen Lord
		this.spriteSet(TFParticleType.AUTUMN_LEAF.get(), List.of(TwilightForestMod.prefix("fey/autumn_brown_particle"), TwilightForestMod.prefix("fey/autumn_brown_particle_01"), TwilightForestMod.prefix("fey/autumn_brown_particle_02"), TwilightForestMod.prefix("fey/autumn_dark_gray_particle_02"), TwilightForestMod.prefix("fey/autumn_dark_gray_particle_01"), TwilightForestMod.prefix("fey/autumn_dark_gray_particle"), TwilightForestMod.prefix("fey/autumn_light_gray_particle_02"), TwilightForestMod.prefix("fey/autumn_light_gray_particle_01"), TwilightForestMod.prefix("fey/autumn_light_gray_particle"), TwilightForestMod.prefix("fey/autumn_red_particle"), TwilightForestMod.prefix("fey/autumn_red_particle_01"), TwilightForestMod.prefix("fey/autumn_red_particle_02")));
		this.spriteSet(TFParticleType.SPRING_LEAF.get(), List.of(TwilightForestMod.prefix("fey/spring_lime_particle"), TwilightForestMod.prefix("fey/spring_lime_particle_01"), TwilightForestMod.prefix("fey/spring_lime_particle_02"), TwilightForestMod.prefix("fey/spring_green_particle"), TwilightForestMod.prefix("fey/spring_green_particle_01"), TwilightForestMod.prefix("fey/spring_green_particle_02"), TwilightForestMod.prefix("fey/spring_cyan_particle"), TwilightForestMod.prefix("fey/spring_cyan_particle_01"), TwilightForestMod.prefix("fey/spring_cyan_particle_02"), TwilightForestMod.prefix("fey/blossom_pink_particle"), TwilightForestMod.prefix("fey/blossom_pink_particle_01"), TwilightForestMod.prefix("fey/blossom_pink_particle_02")));
		this.spriteSet(TFParticleType.SUMMER_LEAF.get(), List.of(TwilightForestMod.prefix("fey/summer_orange_particle"), TwilightForestMod.prefix("fey/summer_orange_particle_01"), TwilightForestMod.prefix("fey/summer_orange_particle_02"), TwilightForestMod.prefix("fey/summer_yellow_particle"), TwilightForestMod.prefix("fey/summer_yellow_particle_01"), TwilightForestMod.prefix("fey/summer_yellow_particle_02")));
		this.spriteSet(TFParticleType.WINTER_LEAF.get(), List.of(TwilightForestMod.prefix("fey/winter_dark_blue_particle"), TwilightForestMod.prefix("fey/winter_dark_blue_particle_01"), TwilightForestMod.prefix("fey/winter_dark_blue_particle_02"), TwilightForestMod.prefix("fey/winter_light_blue_particle"), TwilightForestMod.prefix("fey/winter_light_blue_particle_01"), TwilightForestMod.prefix("fey/winter_light_blue_particle_02"), TwilightForestMod.prefix("fey/blossom_white_particle"), TwilightForestMod.prefix("fey/blossom_white_particle_01"), TwilightForestMod.prefix("fey/blossom_white_particle_02")));
		this.spriteSet(TFParticleType.AUTUMN_SPARKLE.get(), List.of(TwilightForestMod.prefix("fey/sparkle")));
		this.spriteSet(TFParticleType.SNOW_GUARDIAN.get(), TwilightForestMod.prefix("snow"), 4, false);
		this.spriteSet(TFParticleType.SNOW_WARNING.get(), TwilightForestMod.prefix("snow"), 4, false);
		this.sprite(TFParticleType.SORTING_PARTICLE.get(), TwilightForestMod.prefix("log_core"));
		this.sprite(TFParticleType.TRANSFORMATION_PARTICLE.get(), TwilightForestMod.prefix("log_core"));
		this.sprite(TFParticleType.WANDERING_FIREFLY.get(), TwilightForestMod.prefix("firefly"));
		this.sprite(TFParticleType.PURPLE_WANDERING_FIREFLY.get(), TwilightForestMod.prefix("firefly"));
		this.sprite(TFParticleType.OBSIDIAN_MOTE.get(), ResourceLocation.withDefaultNamespace("generic_0"));
		this.sprite(TFParticleType.BLACKSTONE_MOTE.get(), ResourceLocation.withDefaultNamespace("generic_0"));
		this.spriteSet(TFParticleType.DRYING_RACK.get(), ResourceLocation.withDefaultNamespace("generic"), 8, true);
		this.spriteSet(TFParticleType.MAGIC_EFFECT.get(), ResourceLocation.withDefaultNamespace("effect"), 8, true);
		this.sprite(TFParticleType.ANGRY_LICH.get(), ResourceLocation.withDefaultNamespace("angry"));
		this.sprite(TFParticleType.TWILIGHT_ORB.get(), TwilightForestMod.prefix("twilight_orb"));
		this.sprite(TFParticleType.SHIELD_BREAK.get(), TwilightForestMod.prefix("shield_break"));
	}
}

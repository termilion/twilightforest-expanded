package twilightforest.client.particle;

// Ported from Feywild (MIT, SaphieNyako)

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.NotNull;

public class FeySparkleParticle extends TextureSheetParticle {
	protected FeySparkleParticle(ClientLevel world, double x, double y, double z,
							  double vx, double vy, double vz,
							  SpriteSet spriteSet, float red, float green, float blue) {
		super(world, x, y, z, vx, vy, vz);
		this.pickSprite(spriteSet);

		this.rCol = red;
		this.gCol = green;
		this.bCol = blue;

		this.quadSize = 0.05f + (float)Math.random() * 0.05f;
		this.lifetime = 20;
		this.friction = 0.85f;
		this.xd = 0;
		this.yd = 0;
		this.zd = 0;
	}

	@Override
	public @NotNull ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.age >= this.lifetime) {
			this.remove();
		}
	}

	public record Factory(SpriteSet sprites, float red, float green, float blue) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
			return new FeySparkleParticle(level, x, y, z, vx, vy, vz, this.sprites, this.red, this.green, this.blue);
		}
	}
}

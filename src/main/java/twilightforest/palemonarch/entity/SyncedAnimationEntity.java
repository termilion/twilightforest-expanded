package twilightforest.palemonarch.entity;

/**
 * The server writes the clip name into synced data; every side copies it into the field the GeckoLib controllers read.
 */
public interface SyncedAnimationEntity {
	String getSyncedAnimation();

	void setAnimation(String animation);

	void playAnimation(String animation);

	default void syncAnimation() {
		String animation = this.getSyncedAnimation();
		if (!animation.equals("undefined")) {
			this.setAnimation("undefined");
			this.playAnimation(animation);
		}
	}
}

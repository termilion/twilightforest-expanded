package twilightforest.palemonarch.procedures;

import twilightforest.palemonarch.entity.PaleMonarchEntity;
import net.minecraft.world.entity.Entity;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class TiredLookProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : !((PaleMonarchEntity)entity).animationprocedure.equals("Tired")
            && (entity instanceof PaleMonarchEntity _datEntI ? (Integer)_datEntI.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown) : 0) <= 0;
   }
}

package me.iris.ambien.obfuscator.transformers.impl.packaging;

import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import java.time.LocalDate;
import java.time.ZoneOffset;

@SuppressWarnings("Convert2Diamond")
@TransformerInfo(
        name = "metadata",
        category = Category.PACKAGING,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Edit files' metadata."
)
public class Metadata extends Transformer {

    // Fake modification timestamp
    public static final BooleanSetting corruptTime = new BooleanSetting("fake-time", false);

    // Should we set(or override) the archive comment?
    public static final StringSetting setComment = new StringSetting("files-comment", "");

    public static final BooleanSetting forceSetComment = new BooleanSetting("force-comment",true);

    // If we should, what it's content?
    public static final StringSetting commentText = new StringSetting("archive" +
                                                                      "-comment",
                                                                      "你看你看妈呢？");

    // Note: Use miles, so (withoutMileValue) *= 1000
    public static final NumberSetting<Long> modificationTimeStamp = new NumberSetting<Long>("modification-timestamp", LocalDate.of(2022, 2, 24).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());

    @Override
    public void transform(JarWrapper wrapper) {
        // empty
    }
}

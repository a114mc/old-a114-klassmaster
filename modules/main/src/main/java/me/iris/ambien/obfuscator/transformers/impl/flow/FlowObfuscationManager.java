package me.iris.ambien.obfuscator.transformers.impl.flow;

import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@TransformerInfo(
		name = "flow", category = Category.CONTROL_FLOW,
		stability = Stability.STABLE, ordinal = Ordinal.LOW, description = "Flow."
)
public class FlowObfuscationManager extends Transformer {
	public static final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();

    public final BooleanSetting gotoFlow = new BooleanSetting("goto-flow", false);
	public final BooleanSetting c_flow = new BooleanSetting("caesium-flow", false);
	public final BooleanSetting nestedTryCatch = new BooleanSetting("nested-try-catch", false);



	@Override public void transform (JarWrapper wrapper) {
        if (c_flow.isEnabled()) {
            CaesiumFlow.transform(wrapper);
        }
        //
        getClasses(wrapper).forEach(classWrapper -> {
            List<MethodWrapper> methods = new ArrayList<>(
                    classWrapper.getTransformableMethods());
            classMethodsMap.put(classWrapper, methods);
        });

        if (gotoFlow.isEnabled()) {
            GotoFlow.gotoFlow();
        }
        if (nestedTryCatch.isEnabled()) {
            for (ClassWrapper classWrapper : classMethodsMap.keySet()) {
                // TODO randomize depth(based on instruction size
                NestedTryCatch.add(classWrapper.getNode(), 10);
            }
        }
	}
}

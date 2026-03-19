package me.iris.ambien.obfuscator.builders;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.objectweb.asm.tree.MethodNode;

@Getter
@Setter
@Builder
public class MethodBuilder {
    public int access;
    private String name, desc, signature;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @FieldNameConstants.Exclude

    private final String[] exceptions;

    public void addAccess(int flag){
        this.access |= flag;
    }
    public MethodNode buildNode() {
        return new MethodNode(access, name, desc, signature, exceptions);
    }
}

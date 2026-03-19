/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.utilities.kek;

/**
 * Generate the descriptor for ASM
 */
public final class DescriptorGenerator {
	public Class<?>[] args;
	public Class<?>[] returnType;

	public DescriptorGenerator(Class<?>[] args, Class<?>[] returnType) {
		this.args = args;
		this.returnType = returnType;
	}

	public static DescriptorGeneratorBuilder builder() {
		return new DescriptorGeneratorBuilder();
	}

	@Override
	public String toString(){
		return descriptor(true, args) + descriptor(false, returnType);
	}


	private DescriptorGenerator() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	// Thanks qwen
	public String descriptor(boolean isArg, Class<?>... args) {
		StringBuilder sb = new StringBuilder();
		for (Class<?> type : args) {
			sb.append(descriptor(type));
		}
		if (isArg) {
			sb.insert(0, "(");
			sb.append(")");

		}
		return sb.toString();
	}

	public static String descriptor(Class<?> type) {
		// int boolean byte short char long float double void
		if (type.isPrimitive()) {
			if (type == int.class) return "I";
			if (type == boolean.class) return "Z";
			if (type == byte.class) return "B";
			if (type == short.class) return "S";
			if (type == char.class) return "C";
			if (type == long.class) return "J";
			if (type == float.class) return "F";
			if (type == double.class) return "D";
			if (type == void.class) return "V";
			// Impossible to reach this point
			throw new IllegalStateException("Unknown primitive type: " + type);
		}
		if (type.isArray()) {
			// Array descriptor, recursively call descriptor on the component type
			return "[" + descriptor(type.getComponentType());
		}
		// ok bro bruh ahh
		return "L" + type.getCanonicalName().replace('.', '/') + ";";
	}

	// DeLombok moment
	public static class DescriptorGeneratorBuilder {
		private Class<?>[] args;
		private Class<?>[] returnType;

		DescriptorGeneratorBuilder() {
		}

		public DescriptorGeneratorBuilder args(Class<?>... args) {
			this.args = args;
			return this;
		}

		public DescriptorGeneratorBuilder returnType(Class<?>... returnType) {
			this.returnType = returnType;
			return this;
		}

		public DescriptorGenerator build() {
			return new DescriptorGenerator(this.args, this.returnType);
		}
	}
}

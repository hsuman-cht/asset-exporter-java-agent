package com.cloudhealth.asset.cache.exporter.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class AssetExporterCacheAgent {

	private static Logger LOGGER = LoggerFactory.getLogger(AssetExporterCacheAgent.class);

	private static final String CLASS_NAME = "org.apache.log4j.Category";

	public static void premain(String agentArgs, Instrumentation inst) {
		LOGGER.info("[Agent] In premain method");
		transformClass(CLASS_NAME, inst);
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		LOGGER.info("[Agent] In agentmain method");
		transformClass(CLASS_NAME, inst);
	}

	private static void transformClass(String className, Instrumentation instrumentation) {
		Class<?> targetCls = null;
		ClassLoader targetClassLoader = null;
		try {
			targetCls = Class.forName(className);
			targetClassLoader = targetCls.getClassLoader();
			transform(targetCls, targetClassLoader, instrumentation);
			return;
		} catch (Exception ex) {
			LOGGER.error(
					String.format("Exception encountered %s while trying to load the class using Class.forName", ex));
		}
		for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
			if (clazz.getName().equals(className)) {
				targetCls = clazz;
				targetClassLoader = targetCls.getClassLoader();
				transform(targetCls, targetClassLoader, instrumentation);
				return;
			}
		}
		throw new RuntimeException("Failed to find class [" + className + "]");
	}

	private static void transform(Class<?> clazz, ClassLoader classLoader, Instrumentation instrumentation) {
		CategoryTransformer categoryTransformer = new CategoryTransformer(clazz.getName(), classLoader);
		instrumentation.addTransformer(categoryTransformer, true);
		try {
			instrumentation.retransformClasses(clazz);
		} catch (Exception ex) {
			throw new RuntimeException("Transformation failed for class: [" + clazz.getName() + "]", ex);
		}
	}

}
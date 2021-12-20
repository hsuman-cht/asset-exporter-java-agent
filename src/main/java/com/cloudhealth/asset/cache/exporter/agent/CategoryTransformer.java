package com.cloudhealth.asset.cache.exporter.agent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class CategoryTransformer implements ClassFileTransformer {

	private static Logger LOGGER = LoggerFactory.getLogger(CategoryTransformer.class);

	private static final String TARGET_METHOD = "getParent";

	private String targetClassName;

	private ClassLoader targetClassLoader;

	public CategoryTransformer(String targetClassName, ClassLoader targetClassLoader) {
		this.targetClassName = targetClassName;
		this.targetClassLoader = targetClassLoader;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;

		String finalTargetClassName = this.targetClassName.replaceAll("\\.", "/");
		if (!className.equals(finalTargetClassName)) {
			return byteCode;
		}

		if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)) {
			LOGGER.info("[Agent] Transforming class Category");
			try {
				ClassPool cp = ClassPool.getDefault();
				CtClass cc = cp.get(targetClassName);
				CtMethod m = cc.getDeclaredMethod(TARGET_METHOD);

				StringBuilder startBlock = new StringBuilder();
				startBlock.append("return getInstance(\"root\");");
				m.insertBefore(startBlock.toString());

//				m.addLocalVariable("startTime", CtClass.longType);
//				m.insertBefore("startTime = System.currentTimeMillis();");
//				StringBuilder endBlock = new StringBuilder();
//				m.addLocalVariable("endTime", CtClass.longType);
//				m.addLocalVariable("opTime", CtClass.longType);
//				endBlock.append("endTime = System.currentTimeMillis();");
//				endBlock.append("opTime = (endTime-startTime)/1000;");
//				endBlock.append(
//						"LOGGER.info(\"[Application] Withdrawal operation completed in:\" + opTime + \" seconds!\");");
//				m.insertAfter(endBlock.toString());

				byteCode = cc.toBytecode();
				cc.detach();
				LOGGER.info("[Agent] Patched getParent method of class Category");
			} catch (NotFoundException | CannotCompileException | IOException e) {
				LOGGER.error("Exception", e);
			}
		}
		return byteCode;
	}
}
/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * Utility class for handling registration of auto-proxy creators used internally
 * by the '<code>aop</code>' namespace tags.
 *
 * <p>Only a single auto-proxy creator can be registered and multiple tags may wish
 * to register different concrete implementations. As such this class delegates to
 * {@link AopConfigUtils} which wraps a simple escalation protocol. Therefore classes
 * may request a particular auto-proxy creator and know that class, <i>or a subclass
 * thereof</i>, will eventually be resident in the application context.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @see AopConfigUtils
 */
public abstract class AopNamespaceUtils {

	/**
	 * proxy-target-class属性值决定是基于接口的还是基于类的代理被创建。如果proxy-target-class 属性值被设置为true，
	 * 那么基于类的代理将起作用（这时需要cglib库）。
	 * 如果proxy-target-class属值被设置为false或者这个属性被省略，那么标准的JDK 基于接口的代理将起作用
	 * The <code>proxy-target-class</code> attribute as found on AOP-related XML tags.
	 */
	public static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	/**
	 * expose-proxy 当前代理是否为可暴露状态，值是"ture"，则为可访问？？？
	 * The <code>expose-proxy</code> attribute as found on AOP-related XML tags.
	 */
	private static final String EXPOSE_PROXY_ATTRIBUTE = "expose-proxy";


	public static void registerAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {

		BeanDefinition beanDefinition = AopConfigUtils.registerAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	public static void registerAspectJAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {

		BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {
	    // 1.注册或升级AutoProxyCreator定义的beanName为org.Springframework.aop.config.internalAutoProxyCreator的BeanDefinition
	    // AopConfigUtils
		BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		// 2.对 proxy-target-class 和 expose-proxy处理
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		// 3.注册组件并通知，便于监听器做进一步处理
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	/**
	 * @deprecated since Spring 2.5, in favor of
	 * {@link #registerAutoProxyCreatorIfNecessary(ParserContext, Element)} and
	 * {@link AopConfigUtils#registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry, Object)}
	 */
	@Deprecated
	public static void registerAutoProxyCreatorIfNecessary(ParserContext parserContext, Object source) {
		BeanDefinition beanDefinition = AopConfigUtils.registerAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), source);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	/**
	 * @deprecated since Spring 2.5, in favor of
	 * {@link AopConfigUtils#forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry)}
	 */
	@Deprecated
	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
	}


	private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, Element sourceElement) {
		if (sourceElement != null) {
			// proxy-target-class的处理
			boolean proxyTargetClass = Boolean.valueOf(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
			if (proxyTargetClass) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			// expose-proxy的处理
			boolean exposeProxy = Boolean.valueOf(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
			if (exposeProxy) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

	private static void registerComponentIfNecessary(BeanDefinition beanDefinition, ParserContext parserContext) {
		if (beanDefinition != null) {
			BeanComponentDefinition componentDefinition =
					new BeanComponentDefinition(beanDefinition, AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
			parserContext.registerComponent(componentDefinition);
		}
	}

}

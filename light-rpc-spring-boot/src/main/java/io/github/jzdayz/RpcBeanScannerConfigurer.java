package io.github.jzdayz;

import io.github.jzdayz.annotation.RpcClient;
import io.github.jzdayz.client.Client;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class RpcBeanScannerConfigurer implements BeanDefinitionRegistryPostProcessor {

  private BeanFactory beanFactory;


  public RpcBeanScannerConfigurer(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    ClassPathRpcScanner scanner = new ClassPathRpcScanner(registry,
        beanFactory.getBean(Client.class));
    scanner.addIncludeFilter(new AnnotationTypeFilter(RpcClient.class));
    scanner.scan(AutoConfigurationPackages.get(this.beanFactory).toArray(new String[0]));
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {

  }

  public static class ClassPathRpcScanner extends ClassPathBeanDefinitionScanner {

    private Client client;

    public ClassPathRpcScanner(BeanDefinitionRegistry registry, Client client) {
      super(registry);
      this.client = client;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
      Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
      if (beanDefinitionHolders.size() > 0) {
        processBeanDefinitions(beanDefinitionHolders);
      }
      return beanDefinitionHolders;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitionHolders) {

      beanDefinitionHolders.forEach(beanDefinitionHolder -> {
        GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinitionHolder
            .getBeanDefinition();
        String beanClass = definition.getBeanClassName();
        definition.setBeanClass(LightRpcBeanFactory.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClass);
        definition.getConstructorArgumentValues().addGenericArgumentValue(this.client);
      });

    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
      return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata()
          .isIndependent();
    }
  }
}

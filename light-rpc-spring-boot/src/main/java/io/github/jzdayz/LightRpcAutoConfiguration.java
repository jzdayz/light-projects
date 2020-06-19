package io.github.jzdayz;

import io.github.jzdayz.annotation.RpcProvider;
import io.github.jzdayz.client.Client;
import io.github.jzdayz.server.RpcRegister;
import io.github.jzdayz.server.Server;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LightRpcAutoConfiguration implements EnvironmentAware {

    private Environment environment;

    @Bean("light.rpc.client")
    public Client client() {
        return new Client(
                environment.getProperty("rpc.provider.address", "localhost"),
                Integer.parseInt(environment.getProperty("rpc.provider.int", "20090")));
    }

    @Bean
    public RpcBeanScannerConfigurer rpcBeanScannerConfigurer(DefaultListableBeanFactory beanFactory) {
        return new RpcBeanScannerConfigurer(beanFactory);
    }

    @Bean("light.rpc.server")
    public Server server(ApplicationContext applicationContext) {
        Server server = new Server(
                Integer.parseInt(environment.getProperty("rpc.consume.int", "20090")));
        server.start();
        applicationContext.getBeansWithAnnotation(RpcProvider.class).values().forEach(bean ->
                RpcRegister.INSTANCE
                        .registerProvider(bean, bean.getClass().getAnnotation(RpcProvider.class).value()));
        return server;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}

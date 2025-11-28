package br.com.julio.pi.backoffice_users.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Order(2) 
@Component
public class ConsoleRunner implements CommandLineRunner {

    private final ConsoleController console;

    public ConsoleRunner(ConsoleController console) {
        this.console = console;
    }

    @Override
    public void run(String... args) {
        console.iniciarConsole(); 
    }
}

package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.MessageEvent;

@ApplicationScoped
public class MessageEventRepository implements PanacheRepository<MessageEvent> {}

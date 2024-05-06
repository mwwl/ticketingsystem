package com.distalgo.saga.callback;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CallbackRepo extends JpaRepository<CallbackEntity, String> {
}

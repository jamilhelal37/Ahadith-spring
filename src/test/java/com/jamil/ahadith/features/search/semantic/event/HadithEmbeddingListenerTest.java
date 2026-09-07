package com.jamil.ahadith.features.search.semantic.event;

import com.jamil.ahadith.features.search.semantic.service.HadithEmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class HadithEmbeddingListenerTest {
    @Test
    void listenerIsAfterCommitAndDelegatesWithoutPropagatingFailures() throws Exception {
        Method method = HadithEmbeddingListener.class.getMethod("onHadithTextChanged", HadithTextChangedEvent.class);
        assertThat(method.getAnnotation(TransactionalEventListener.class).phase())
                .isEqualTo(TransactionPhase.AFTER_COMMIT);

        HadithEmbeddingService service = mock(HadithEmbeddingService.class);
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("offline")).when(service).embedHadith(id, "text");
        HadithEmbeddingListener listener = new HadithEmbeddingListener(service);

        assertThatCode(() -> listener.onHadithTextChanged(new HadithTextChangedEvent(id, "text")))
                .doesNotThrowAnyException();
    }
}

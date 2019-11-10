package com.github.hcsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MultiThreadServiceDataProcessorTest {
    @Mock RemoteService remoteService;

    @Test
    public void successIfAllThreadsAreGood() {
        MultiThreadServiceDataProcessor processor =
                new MultiThreadServiceDataProcessor(10, remoteService);
        Assertions.assertTrue(processor.processAllData(Collections.nCopies(10, new Object())));
    }

    @Test
    public void failureIfAThreadIsBad() {
        MultiThreadServiceDataProcessor processor =
                new MultiThreadServiceDataProcessor(10, remoteService);

        Object badData = new Object();
        Mockito.doThrow(new IllegalStateException()).when(remoteService).processData(badData);

        List<Object> allData = new ArrayList<>(Collections.nCopies(9, new Object()));
        allData.add(badData);
        Assertions.assertFalse(processor.processAllData(allData));
    }
}

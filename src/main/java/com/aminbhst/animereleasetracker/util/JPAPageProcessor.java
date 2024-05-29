package com.aminbhst.animereleasetracker.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Slf4j
public abstract class JPAPageProcessor<T> {

    public void process() {
        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<T> page = fetch(pageRequest);
        while (!page.isEmpty()) {
            pageRequest = pageRequest.next();
            for (final T item : page) {
                try {
                    processItem(item);
                } catch (Throwable t) {
                    log.error("Error occurred while processing item {}", item.toString(), t);
                }
            }
            page = fetch(pageRequest);
        }
    }

    public abstract Page<T> fetch(PageRequest pageRequest);

    public abstract void processItem(T item) throws Exception;
}

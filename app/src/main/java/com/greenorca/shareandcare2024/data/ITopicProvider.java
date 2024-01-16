package com.greenorca.shareandcare2024.data;

import java.util.Set;

public interface ITopicProvider {

    Set<String> getRootTopics();
    Set<String> getSubTopics(String topic);
    void addTopic(String topic, String parent);

}

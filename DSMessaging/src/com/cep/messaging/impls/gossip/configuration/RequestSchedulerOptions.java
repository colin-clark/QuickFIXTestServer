package com.cep.messaging.impls.gossip.configuration;

import java.util.Map;
/**
 *
 */
public class RequestSchedulerOptions
{
    public static final Integer DEFAULT_THROTTLE_LIMIT = 80;
    public static final Integer DEFAULT_WEIGHT = 1;

    public Integer throttle_limit = DEFAULT_THROTTLE_LIMIT;
    public Integer default_weight = DEFAULT_WEIGHT;
    public Map<String, Integer> weights;
}

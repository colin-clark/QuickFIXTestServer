package com.cep.messaging.impls.gossip.keyspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.cep.messaging.impls.gossip.replication.AbstractReplicationStrategy;
import com.cep.messaging.impls.gossip.replication.NetworkTopologyStrategy;
import com.cep.messaging.impls.gossip.thrift.CfDef;
import com.cep.messaging.impls.gossip.thrift.KsDef;
import com.cep.messaging.util.exception.ConfigurationException;

public final class KSMetaData
{
    public final String name;
    public final Class<? extends AbstractReplicationStrategy> strategyClass;
    public final Map<String, String> strategyOptions;
    private final Map<String, CFMetaData> cfMetaData;
    private boolean durable_writes;

    public KSMetaData(String name, Class<? extends AbstractReplicationStrategy> strategyClass, Map<String, String> strategyOptions, CFMetaData... cfDefs)
    {
        this(name, strategyClass, strategyOptions, true, cfDefs);
    }

    public KSMetaData(String name, Class<? extends AbstractReplicationStrategy> strategyClass, Map<String, String> strategyOptions, boolean durable_writes, CFMetaData... cfDefs)
    {
        this.name = name;
        this.strategyClass = strategyClass == null ? NetworkTopologyStrategy.class : strategyClass;
        this.strategyOptions = strategyOptions;
        Map<String, CFMetaData> cfmap = new HashMap<String, CFMetaData>();
        for (CFMetaData cfm : cfDefs)
            cfmap.put(cfm.cfName, cfm);
        this.cfMetaData = Collections.unmodifiableMap(cfmap);
        this.durable_writes = durable_writes;
    }
    
    public void setDurableWrites(boolean durable_writes)
    {
        this.durable_writes = durable_writes;
    }
    
    public boolean isDurableWrites()
    {
        return durable_writes;
    }
    
    @SuppressWarnings("deprecation")
	public static Map<String, String> forwardsCompatibleOptions(KsDef ks_def)
    {
        Map<String, String> options;
        options = ks_def.strategy_options == null
                ? new HashMap<String, String>()
                : new HashMap<String, String>(ks_def.strategy_options);
        maybeAddReplicationFactor(options, ks_def.strategy_class, ks_def.isSetReplication_factor() ? ks_def.replication_factor : null);
        return options;
    }

    private static void maybeAddReplicationFactor(Map<String, String> options, String cls, Integer rf)
    {
        if (rf != null && (cls.endsWith("SimpleStrategy") || cls.endsWith("OldNetworkTopologyStrategy")))
            options.put("replication_factor", rf.toString());
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof KSMetaData))
            return false;
        KSMetaData other = (KSMetaData)obj;
        return other.name.equals(name)
                && ObjectUtils.equals(other.strategyClass, strategyClass)
                && ObjectUtils.equals(other.strategyOptions, strategyOptions)
                && other.cfMetaData.size() == cfMetaData.size()
                && other.cfMetaData.equals(cfMetaData)
                && other.durable_writes == durable_writes;
    }

    public Map<String, CFMetaData> cfMetaData()
    {
        return cfMetaData;
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(name)
          .append("rep strategy:")
          .append(strategyClass.getSimpleName())
          .append("{")
          .append(StringUtils.join(cfMetaData.values(), ", "))
          .append("}")
          .append("durable_writes: ").append(durable_writes);
        return sb.toString();
    }

    public static String convertOldStrategyName(String name)
    {
        return name.replace("RackUnawareStrategy", "SimpleStrategy")
                   .replace("RackAwareStrategy", "OldNetworkTopologyStrategy");
    }

    public static Map<String,String> optsWithRF(final Integer rf)
    {
        Map<String, String> ret = new HashMap<String,String>();
        ret.put("replication_factor", rf.toString());
        return ret;
    }

    public static KSMetaData fromThrift(KsDef ksd, CFMetaData... cfDefs) throws ConfigurationException
    {
        return new KSMetaData(ksd.name,
                              AbstractReplicationStrategy.getClass(ksd.strategy_class),
                              forwardsCompatibleOptions(ksd),
                              ksd.durable_writes,
                              cfDefs);
    }

    @SuppressWarnings("deprecation")
	public static KsDef toThrift(KSMetaData ksm)
    {
        List<CfDef> cfDefs = new ArrayList<CfDef>();
        for (CFMetaData cfm : ksm.cfMetaData().values())
            cfDefs.add(CFMetaData.convertToThrift(cfm));
        KsDef ksdef = new KsDef(ksm.name, ksm.strategyClass.getName(), cfDefs);
        ksdef.setStrategy_options(ksm.strategyOptions);
        if (ksm.strategyOptions != null && ksm.strategyOptions.containsKey("replication_factor"))
            ksdef.setReplication_factor(Integer.parseInt(ksm.strategyOptions.get("replication_factor")));
        ksdef.durable_writes = ksm.durable_writes;
        
        return ksdef;
    }
}

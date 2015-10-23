package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.applicationmap.Link;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by Naver on 2015-10-21.
 */
public class TransactionInfoViewModel {

    private TransactionId traceId;
    private Collection<Node> nodes;
    private Collection<Link> links;
    private RecordSet recordSet;
    private String completeState;
    private boolean logLinkEnable;
    private String logButtonName;
    private String logPageUrl;
    private String disableButtonMessage;

    public TransactionInfoViewModel(TransactionId traceId, Collection<Node> nodes, Collection<Link> links, RecordSet recordSet, String completeState, boolean logLinkEnable, String logButtonName, String logPageUrl, String disableButtonMessage) {
        this.traceId = traceId;
        this.nodes = nodes;
        this.links = links;
        this.recordSet = recordSet;
        this.completeState = completeState;
        this.logLinkEnable = logLinkEnable;
        this.logButtonName = logButtonName;
        this.logPageUrl = logPageUrl;
        this.disableButtonMessage = disableButtonMessage;
    }

    @JsonProperty("applicationName")
    public String getApplicationName() {
        return recordSet.getApplicationName();
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return traceId.getFormatString();
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return recordSet.getAgentId();
    }

    @JsonProperty("applicationId")
    public String getApplicationId() {
        return recordSet.getApplicationId();
    }

    @JsonProperty("callStackStart")
    public long getCallStackStart() {
        return recordSet.getStartTime();
    }

    @JsonProperty("callStackEnd")
    public long getCallStackEnd() {
        return recordSet.getEndTime();
    }

    @JsonProperty("completeState")
    public String getCompleteState() {
        return completeState;
    }

    @JsonProperty("logLinkEnable")
    public boolean isLogLinkEnable() {
        return logLinkEnable;
    }

    @JsonProperty("loggingTransactionInfo")
    public boolean isLoggingTransactionInfo() {
        return recordSet.isLoggingTransactionInfo();
    }

    @JsonProperty("logButtonName")
    public String getLogButtonName() {
        return logButtonName;
    }

    @JsonProperty("logPageUrl")
    public String getLogPageUrl() {
        if (logPageUrl != null || logPageUrl.length() > 0) {
            try {
                URL url = new URL(logPageUrl);
                StringBuilder sb = new StringBuilder();
                sb.append("transactionId=").append(traceId.getFormatString());
                sb.append("&time=").append(recordSet.getStartTime());
                if (url.getQuery() != null) {
                    return logPageUrl + "&" + sb.toString();
                } else {
                    return logPageUrl + "?" + sb.toString();
                }
            } catch (MalformedURLException ignored) {
            }
        }

        return "";
    }

    @JsonProperty("disableButtonMessage")
    public String getDisableButtonMessage() {
        return disableButtonMessage;
    }

    @JsonProperty("callStackIndex")
    public Map<String, Integer> getCallStackIndex() {
        final Map<String, Integer> index = new HashMap<String, Integer>();
        for (int i = 0; i < CallStack.INDEX.length; i++) {
            index.put(CallStack.INDEX[i], i);
        }

        return index;
    }

    @JsonProperty("callStack")
    public List<CallStack> getCallStack() {

        List<CallStack> list = new ArrayList<CallStack>();
        boolean first = true;
        long barRatio = 0;
        for(Record record : recordSet.getRecordList()) {
            if(first) {
                long begin = record.getBegin();
                long end = record.getBegin() + record.getElapsed();
                if(end  - begin > 0) {
                    barRatio = 100 / (end - begin);
                }
                first = false;
            }
            list.add(new CallStack(record, barRatio));
        }

        return list;
    }

    @JsonProperty("applicationMapData")
    public Map<String, List<Object>> getApplicationMapData() {
        Map<String, List<Object>> result = new HashMap<String, List<Object>>();
        List<Object> nodeDataArray = new ArrayList<Object>();
        for(Node node : nodes) {
            nodeDataArray.add(node.getNodeJson());
        }
        result.put("nodeDataArray", nodeDataArray);
        List<Object> linkDataArray = new ArrayList<Object>();
        for(Link link : links) {
            linkDataArray.add(link.getJson());
        }
        result.put("linkDataArray", linkDataArray);

        return result;
    }

    @JsonSerialize(using=TransactionInfoCallStackSerializer.class)
    public static class CallStack {
        static final String[] INDEX = {"depth",
                "begin",
                "end",
                "excludeFromTimeline",
                "applicationName",
                "tab",
                "id",
                "parentId",
                "isMethod",
                "hasChild",
                "title",
                "arguments",
                "executeTime",
                "gap",
                "elapsedTime",
                "barWidth",
                "executionMilliseconds",
                "simpleClassName",
                "methodType",
                "apiType",
                "agent",
                "isFocused",
                "hasException",
                "logButtonName",
                "logPageUrl"
        };

        private String depth = "";
        private long begin;
        private long end;
        private boolean excludeFromTimeline;
        private String applicationName = "";
        private int tab;
        private String id = "";
        private String parentId = "";
        private boolean isMethod;
        private boolean hasChild;
        private String title = "";
        private String arguments = "";
        private String executeTime = "";
        private String gap = "";
        private String elapsedTime = "";
        private String barWidth = "";
        private String executionMilliseconds = "";
        private String simpleClassName = "";
        private String methodType = "";
        private String apiType = "";
        private String agent = "";
        private boolean isFocused;
        private boolean hasException;
        private String logButtonName = "";
        private String logPageUrl = "";

        public CallStack(final Record record, long barRatio) {
            begin = record.getBegin();
            end = record.getBegin() + record.getElapsed();
            excludeFromTimeline = record.isExcludeFromTimeline();
            applicationName = record.getApplicationName();
            tab = record.getTab();
            id = String.valueOf(record.getId());
            if (record.getParentId() > 0) {
                parentId = String.valueOf(record.getParentId());
            }
            isMethod = record.isMethod();
            hasChild = record.getHasChild();
            title = JSONObject.escape(record.getTitle());
            arguments = JSONObject.escape(StringEscapeUtils.escapeHtml4(record.getArguments()));
            if (record.isMethod()) {
                executeTime = DateUtils.longToDateStr(record.getBegin(), "HH:mm:ss SSS"); // time format
                gap = String.valueOf(record.getGap());
                elapsedTime = String.valueOf(record.getElapsed());
                barWidth = String.format("%1d", (int)(((end - begin) * barRatio) + 0.9));
                executionMilliseconds = String.valueOf(record.getExecutionMilliseconds());
            }
            simpleClassName = record.getSimpleClassName();
            methodType = String.valueOf(record.getMethodType());
            apiType = record.getApiType();
            agent = record.getAgent();
            isFocused = record.isFocused();
            hasException = record.getHasException();
            logButtonName = record.getLogButtonName();

            if (record.getLogPageUrl() != null && record.getLogPageUrl().length() > 0) {
                try {
                    URL url = new URL(record.getLogPageUrl());
                    StringBuilder sb = new StringBuilder();
                    sb.append("transactionId=").append(record.getTransactionId());
                    sb.append("&spanId=").append(record.getSpanId());
                    sb.append("&time=").append(record.getBegin());
                    if (url.getQuery() != null) {
                        logPageUrl = record.getLogPageUrl() + "&" + sb.toString();
                    } else {
                        logPageUrl = record.getLogPageUrl() + "?" + sb.toString();
                    }
                } catch (MalformedURLException ignored) {
                }
            }
        }

        public String getDepth() {
            return depth;
        }

        public long getBegin() {
            return begin;
        }

        public long getEnd() {
            return end;
        }

        public boolean isExcludeFromTimeline() {
            return excludeFromTimeline;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public int getTab() {
            return tab;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return parentId;
        }

        public boolean isMethod() {
            return isMethod;
        }

        public boolean isHasChild() {
            return hasChild;
        }

        public String getTitle() {
            return title;
        }

        public String getArguments() {
            return arguments;
        }

        public String getExecuteTime() {
            return executeTime;
        }

        public String getGap() {
            return gap;
        }

        public String getElapsedTime() {
            return elapsedTime;
        }

        public String getBarWidth() {
            return barWidth;
        }

        public String getExecutionMilliseconds() {
            return executionMilliseconds;
        }

        public String getSimpleClassName() {
            return simpleClassName;
        }

        public String getMethodType() {
            return methodType;
        }

        public String getApiType() {
            return apiType;
        }

        public String getAgent() {
            return agent;
        }

        public boolean isFocused() {
            return isFocused;
        }

        public boolean isHasException() {
            return hasException;
        }

        public String getLogButtonName() {
            return logButtonName;
        }

        public String getLogPageUrl() {
            return logPageUrl;
        }
    }
}
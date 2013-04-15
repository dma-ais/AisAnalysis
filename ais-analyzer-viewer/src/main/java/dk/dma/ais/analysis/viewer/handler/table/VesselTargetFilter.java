package dk.dma.ais.analysis.viewer.handler.table;

import java.util.Set;

import dk.dma.ais.analysis.common.web.QueryParams;
import dk.dma.ais.data.AisClassATarget;
import dk.dma.ais.data.AisTarget;
import dk.dma.ais.data.AisVesselTarget;

public class VesselTargetFilter extends TargetFilter {

    private static final String[] filterNames = { "vesselClass", "staticReport" };

    public VesselTargetFilter(QueryParams request) {
        super(request);
        makeFilterMap(filterNames, request);
    }

    public boolean rejectedByFilter(AisVesselTarget target) {
        Set<String> vesselClass = filterMap.get("vesselClass");
        if (vesselClass != null) {
            String vc = (target instanceof AisClassATarget) ? "A" : "B";
            if (!vesselClass.contains(vc)) {
                return true;
            }
        }
        Set<String> staticReport = filterMap.get("staticReport");
        if (staticReport != null) {
            boolean hasStatic = (target.getVesselStatic() != null);
            if (staticReport.contains("yes") && !hasStatic) {
                return true;
            }
            if (staticReport.contains("no") && hasStatic) {
                return true;
            }
        }
        return super.rejectedByFilter((AisTarget)target);
    }

}

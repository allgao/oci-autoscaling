package ociautoscaling.Model;

import com.oracle.bmc.core.model.Subnet;

public class SubnetAndInstanceCount{
    private Subnet sn;
    private int count;

    public SubnetAndInstanceCount(Subnet sn, int count) {
        this.sn = sn;
        this.count = count;
    }

    public Subnet getSn() {
        return sn;
    }

    public void setSn(Subnet sn) {
        this.sn = sn;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

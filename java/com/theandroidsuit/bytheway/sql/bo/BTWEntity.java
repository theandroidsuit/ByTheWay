package com.theandroidsuit.bytheway.sql.bo;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public abstract class BTWEntity {
    protected long id = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}


package com.wxxiaomi.ming.electricbicycle.ui.activity.view;

import com.wxxiaomi.ming.electricbicycle.ui.presenter.base.BasePre;
import com.wxxiaomi.ming.electricbicycle.ui.activity.base.BaseView;


/**
 * Created by 12262 on 2016/6/9.
 */
public interface SearchView extends BaseView {
//    void setListAdapter(PoiSearchResultAdapter sugAdapter);
    void runRoutePlanAct();
    void setNoResult(boolean flag);
}

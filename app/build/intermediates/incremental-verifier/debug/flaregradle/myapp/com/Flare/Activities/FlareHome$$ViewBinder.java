// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class FlareHome$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.FlareHome> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558476, "field '_searchLocationText'");
    target._searchLocationText = finder.castView(view, 2131558476, "field '_searchLocationText'");
    view = finder.findRequiredView(source, 2131558438, "field '_drawerLayout'");
    target._drawerLayout = finder.castView(view, 2131558438, "field '_drawerLayout'");
    view = finder.findRequiredView(source, 2131558459, "field '_drawerList'");
    target._drawerList = finder.castView(view, 2131558459, "field '_drawerList'");
  }

  @Override public void unbind(T target) {
    target._searchLocationText = null;
    target._drawerLayout = null;
    target._drawerList = null;
  }
}

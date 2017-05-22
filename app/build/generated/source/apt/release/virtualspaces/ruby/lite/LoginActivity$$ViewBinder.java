// Generated code from Butter Knife. Do not modify!
package virtualspaces.ruby.lite;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class LoginActivity$$ViewBinder<T extends virtualspaces.ruby.lite.LoginActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558519, "field 'imagePager'");
    target.imagePager = finder.castView(view, 2131558519, "field 'imagePager'");
    view = finder.findRequiredView(source, 2131558520, "field 'pager_indicator'");
    target.pager_indicator = finder.castView(view, 2131558520, "field 'pager_indicator'");
    view = finder.findRequiredView(source, 2131558521, "field 'go'");
    target.go = finder.castView(view, 2131558521, "field 'go'");
  }

  @Override public void unbind(T target) {
    target.imagePager = null;
    target.pager_indicator = null;
    target.go = null;
  }
}

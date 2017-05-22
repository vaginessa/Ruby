// Generated code from Butter Knife. Do not modify!
package virtualspaces.ruby.lite;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MyLauncher$$ViewBinder<T extends virtualspaces.ruby.lite.MyLauncher> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558517, "field 'animView'");
    target.animView = finder.castView(view, 2131558517, "field 'animView'");
    view = finder.findRequiredView(source, 2131558518, "field 'rubyText'");
    target.rubyText = finder.castView(view, 2131558518, "field 'rubyText'");
  }

  @Override public void unbind(T target) {
    target.animView = null;
    target.rubyText = null;
  }
}

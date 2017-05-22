// Generated code from Butter Knife. Do not modify!
package virtualspaces.ruby.lite;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends virtualspaces.ruby.lite.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558527, "field 'refreshLayout'");
    target.refreshLayout = finder.castView(view, 2131558527, "field 'refreshLayout'");
    view = finder.findRequiredView(source, 2131558529, "field 'fab'");
    target.fab = finder.castView(view, 2131558529, "field 'fab'");
    view = finder.findRequiredView(source, 2131558530, "field 'prog'");
    target.prog = finder.castView(view, 2131558530, "field 'prog'");
    view = finder.findRequiredView(source, 2131558531, "field 'reconnect'");
    target.reconnect = finder.castView(view, 2131558531, "field 'reconnect'");
    view = finder.findRequiredView(source, 2131558528, "field 'mRecyclerView'");
    target.mRecyclerView = finder.castView(view, 2131558528, "field 'mRecyclerView'");
  }

  @Override public void unbind(T target) {
    target.refreshLayout = null;
    target.fab = null;
    target.prog = null;
    target.reconnect = null;
    target.mRecyclerView = null;
  }
}

// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingsActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.SettingsActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558478, "field 'textMessageCheckBox'");
    target.textMessageCheckBox = finder.castView(view, 2131558478, "field 'textMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558476, "field 'cloudMessageCheckBox'");
    target.cloudMessageCheckBox = finder.castView(view, 2131558476, "field 'cloudMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558475, "field 'selectAllowSaveCheckBox'");
    target.selectAllowSaveCheckBox = finder.castView(view, 2131558475, "field 'selectAllowSaveCheckBox'");
    view = finder.findRequiredView(source, 2131558477, "field 'selectFindFriendsCheckBox'");
    target.selectFindFriendsCheckBox = finder.castView(view, 2131558477, "field 'selectFindFriendsCheckBox'");
    view = finder.findRequiredView(source, 2131558425, "field 'currentPhoneNumberTextView'");
    target.currentPhoneNumberTextView = finder.castView(view, 2131558425, "field 'currentPhoneNumberTextView'");
    view = finder.findRequiredView(source, 2131558494, "field 'updateCurrentNumberButton'");
    target.updateCurrentNumberButton = finder.castView(view, 2131558494, "field 'updateCurrentNumberButton'");
  }

  @Override public void unbind(T target) {
    target.textMessageCheckBox = null;
    target.cloudMessageCheckBox = null;
    target.selectAllowSaveCheckBox = null;
    target.selectFindFriendsCheckBox = null;
    target.currentPhoneNumberTextView = null;
    target.updateCurrentNumberButton = null;
  }
}

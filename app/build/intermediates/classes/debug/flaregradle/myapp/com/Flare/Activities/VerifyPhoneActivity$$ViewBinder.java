// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class VerifyPhoneActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.VerifyPhoneActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558498, "field '_verifyPhoneLayout'");
    target._verifyPhoneLayout = finder.castView(view, 2131558498, "field '_verifyPhoneLayout'");
    view = finder.findRequiredView(source, 2131558442, "field '_enterCodeLayout'");
    target._enterCodeLayout = finder.castView(view, 2131558442, "field '_enterCodeLayout'");
    view = finder.findRequiredView(source, 2131558422, "field '_continueLayout'");
    target._continueLayout = finder.castView(view, 2131558422, "field '_continueLayout'");
    view = finder.findRequiredView(source, 2131558424, "field '_countryCodeView'");
    target._countryCodeView = finder.castView(view, 2131558424, "field '_countryCodeView'");
    view = finder.findRequiredView(source, 2131558467, "field '_phoneNumberEntry'");
    target._phoneNumberEntry = finder.castView(view, 2131558467, "field '_phoneNumberEntry'");
    view = finder.findRequiredView(source, 2131558414, "field '_codeEntry'");
    target._codeEntry = finder.castView(view, 2131558414, "field '_codeEntry'");
    view = finder.findRequiredView(source, 2131558443, "field '_verifyErrorMessage'");
    target._verifyErrorMessage = finder.castView(view, 2131558443, "field '_verifyErrorMessage'");
    view = finder.findRequiredView(source, 2131558494, "field '_submitErrorMessage'");
    target._submitErrorMessage = finder.castView(view, 2131558494, "field '_submitErrorMessage'");
    view = finder.findRequiredView(source, 2131558497, "field '_verifyButton'");
    target._verifyButton = finder.castView(view, 2131558497, "field '_verifyButton'");
    view = finder.findRequiredView(source, 2131558472, "field '_progressBar'");
    target._progressBar = finder.castView(view, 2131558472, "field '_progressBar'");
    view = finder.findRequiredView(source, 2131558423, "field '_countryList'");
    target._countryList = finder.castView(view, 2131558423, "field '_countryList'");
  }

  @Override public void unbind(T target) {
    target._verifyPhoneLayout = null;
    target._enterCodeLayout = null;
    target._continueLayout = null;
    target._countryCodeView = null;
    target._phoneNumberEntry = null;
    target._codeEntry = null;
    target._verifyErrorMessage = null;
    target._submitErrorMessage = null;
    target._verifyButton = null;
    target._progressBar = null;
    target._countryList = null;
  }
}

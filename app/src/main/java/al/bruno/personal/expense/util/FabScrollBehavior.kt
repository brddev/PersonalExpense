package al.bruno.personal.expense.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabScrollBehavior (context: Context, attrs: AttributeSet): FloatingActionButton.Behavior(context, attrs) {
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        //return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }
    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        if (dyConsumed > 0 && child.visibility == View.VISIBLE) {
            child.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                @SuppressLint("RestrictedApi")
                override fun onHidden(fab: FloatingActionButton) {
                    super.onHidden(fab)
                    fab.visibility = View.INVISIBLE
                }
            })
            //child.animate().translationY((child.height + (child.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin).toFloat()).setInterpolator(LinearInterpolator()).start()
        } else if (dyConsumed < 0 && child.visibility != View.VISIBLE) {
            child.show()
            //child.animate().translationY(0.toFloat()).setInterpolator(LinearInterpolator()).start();
        }
    }

    /*override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, type: Int) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        Handler().postDelayed({
            child.show()
            //child.animate().translationY(0.toFloat()).setInterpolator(LinearInterpolator()).start()
        }, 1000)
    }*/
}

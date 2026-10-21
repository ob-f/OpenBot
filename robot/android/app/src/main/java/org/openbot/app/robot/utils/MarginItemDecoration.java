package org.openbot.app.robot.utils;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
  private final int spaceHeight;

  public MarginItemDecoration(int spaceHeight) {
    this.spaceHeight = spaceHeight;
  }

  @Override
  public void getItemOffsets(
      @NonNull Rect outRect,
      @NonNull View view,
      @NonNull RecyclerView parent,
      @NonNull RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    outRect.left = spaceHeight;
    outRect.right =
        (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) ? spaceHeight : 0;
    outRect.bottom = spaceHeight / 2;
  }
}

package jatran.stub

class ForLoopsChangeToWhileLoops {
  def forLoopsChangeToWhileLoops():unit = {
    var i: int = 0

    while (i < 10) {
      System.out.println("we are looping the loop")
      i = i + 1
    }
  }
}

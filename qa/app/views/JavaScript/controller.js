var questionsList = #{jsAction @Questions.home() /}
var questionsGet = #{jsAction @Questions.question(':id') /}
var voteUp = #{jsAction @Questions.voteUp(':id') /}
var voteDown = #{jsAction @Questions.voteDown(':id') /}
var removeVote = #{jsAction @Questions.removeVote(':id') /}
var setBestAnswer = #{jsAction @Questions.setBestAnswer(':id') /}
var resetBestAnswer = #{jsAction @Questions.resetBestAnswer(':id') /}
var hot = #{jsAction @Questions.hot() /}
var mine = #{jsAction @Questions.mine() /}
var search = #{jsAction @Questions.search(':string') /}
var form = #{jsAction @Questions.form(':type') /}
var add = #{jsAction @Questions.add(':title',':content',':tags') /}
package ca.usask.chdp.ExpServerCore.IntegrationTests


case class TestChatMsg(sentOnRound: Int, sentByID: String, sentTo: String,
                       roleSender: String,
                       roleSentTo: String, msg: String)


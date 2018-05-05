# Useful Logs

## Multicast over Weave Net + Overlay

```bash
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:42.520478]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Grace Hopper', messageOrigin=MessageOrigin{host='f456f855d5e9', ip='10.0.4.9', name='Grace Hopper'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:42.525351]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Grace Hopper', messageOrigin=MessageOrigin{host='f456f855d5e9', ip='10.0.4.9', name='Grace Hopper'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:42.544152]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='1bdafe04b4e0', ip='10.0.4.10', name='Alan Kay'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:42.544237]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='1bdafe04b4e0', ip='10.0.4.10', name='Alan Kay'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:43.303379]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=26, type=MEMBERSHIP, message='Hello from Tim Berners-Lee', messageOrigin=MessageOrigin{host='8b058c0af412', ip='10.0.2.114', name='Tim Berners-Lee'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:43.303265]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=26, type=MEMBERSHIP, message='Hello from Tim Berners-Lee', messageOrigin=MessageOrigin{host='8b058c0af412', ip='10.0.2.114', name='Tim Berners-Lee'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:43.298362]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=26, type=MEMBERSHIP, message='Hello from Tim Berners-Lee', messageOrigin=MessageOrigin{host='8b058c0af412', ip='10.0.2.114', name='Tim Berners-Lee'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:43.674152]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=25, type=MEMBERSHIP, message='Hello from Linus Torvalds', messageOrigin=MessageOrigin{host='546b77607582', ip='10.0.4.16', name='Linus Torvalds'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:43.678943]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=25, type=MEMBERSHIP, message='Hello from Linus Torvalds', messageOrigin=MessageOrigin{host='546b77607582', ip='10.0.4.16', name='Linus Torvalds'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:43.678880]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=25, type=MEMBERSHIP, message='Hello from Linus Torvalds', messageOrigin=MessageOrigin{host='546b77607582', ip='10.0.4.16', name='Linus Torvalds'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:46.390127]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=25, type=MEMBERSHIP, message='Hello from Leslie Lamport', messageOrigin=MessageOrigin{host='e191f5f452a7', ip='10.0.4.17', name='Leslie Lamport'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:46.384950]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=25, type=MEMBERSHIP, message='Hello from Leslie Lamport', messageOrigin=MessageOrigin{host='e191f5f452a7', ip='10.0.4.17', name='Leslie Lamport'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:46.390079]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=25, type=MEMBERSHIP, message='Hello from Leslie Lamport', messageOrigin=MessageOrigin{host='e191f5f452a7', ip='10.0.4.17', name='Leslie Lamport'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:46.707208]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Brendan Eich', messageOrigin=MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:46.712165]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Brendan Eich', messageOrigin=MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:46.711542]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Brendan Eich', messageOrigin=MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:46.731285]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=27, type=MEMBERSHIP, message='Hello from John von Neumann', messageOrigin=MessageOrigin{host='0d854beebb0a', ip='10.0.4.14', name='John von Neumann'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:46.736183]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=27, type=MEMBERSHIP, message='Hello from John von Neumann', messageOrigin=MessageOrigin{host='0d854beebb0a', ip='10.0.4.14', name='John von Neumann'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:46.735481]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=27, type=MEMBERSHIP, message='Hello from John von Neumann', messageOrigin=MessageOrigin{host='0d854beebb0a', ip='10.0.4.14', name='John von Neumann'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:46.851866]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='d4cf9008c514', ip='10.0.1.224', name='Alan Kay'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:46.847005]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='d4cf9008c514', ip='10.0.1.224', name='Alan Kay'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:46.851932]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='d4cf9008c514', ip='10.0.1.224', name='Alan Kay'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:47.475776]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=27, type=MEMBERSHIP, message='Hello from John von Neumann', messageOrigin=MessageOrigin{host='abe764415a8f', ip='10.0.0.116', name='John von Neumann'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:47.479900]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=27, type=MEMBERSHIP, message='Hello from John von Neumann', messageOrigin=MessageOrigin{host='abe764415a8f', ip='10.0.0.116', name='John von Neumann'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:47.525850]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Grace Hopper', messageOrigin=MessageOrigin{host='f456f855d5e9', ip='10.0.4.9', name='Grace Hopper'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:47.520883]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=23, type=MEMBERSHIP, message='Hello from Grace Hopper', messageOrigin=MessageOrigin{host='f456f855d5e9', ip='10.0.4.9', name='Grace Hopper'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:47.544815]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='1bdafe04b4e0', ip='10.0.4.10', name='Alan Kay'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:47.544741]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=19, type=MEMBERSHIP, message='Hello from Alan Kay', messageOrigin=MessageOrigin{host='1bdafe04b4e0', ip='10.0.4.10', name='Alan Kay'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[INFO]	[21:06:47.986518]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=35, type=MEMBERSHIP, message='So Long and Thanks for All the Fish', messageOrigin=MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}}
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[WARN]	[21:06:47.986669]	[15]	[Main]				Received membership leave notice from MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}
buming-1_dui.1.qnlvngzhighy@dui-4    | com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException: Could not send message to 7781
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.propagateMembershipLeaveNotice(DistributedServer.java:125)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.updateMemberShipList(DistributedServer.java:96)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.lambda$listenToInternalCommunication$0(DistributedServer.java:74)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:514)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
buming-1_dui.1.qnlvngzhighy@dui-4    | 	at java.base/java.lang.Thread.run(Thread.java:844)
buming-1_dui.1.qnlvngzhighy@dui-4    | [Server-John von Neumann]		[ERROR]	[21:06:47.993542]	[15]	[Main]				Could not send message to 7781
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:47.986347]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=35, type=MEMBERSHIP, message='So Long and Thanks for All the Fish', messageOrigin=MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[WARN]	[21:06:47.986494]	[15]	[Main]				Received membership leave notice from MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}
buming-1_dui.2.lxqo3wd6z1to@dui-5    | com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException: Could not send message to 7781
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.propagateMembershipLeaveNotice(DistributedServer.java:125)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.updateMemberShipList(DistributedServer.java:96)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.lambda$listenToInternalCommunication$0(DistributedServer.java:74)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:514)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | 	at java.base/java.lang.Thread.run(Thread.java:844)
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[ERROR]	[21:06:47.991597]	[15]	[Main]				Could not send message to 7781
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:47.982657]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=35, type=MEMBERSHIP, message='So Long and Thanks for All the Fish', messageOrigin=MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[WARN]	[21:06:47.982783]	[16]	[Main]				Received membership leave notice from MessageOrigin{host='bc5b21ad6f8e', ip='10.0.4.12', name='Brendan Eich'}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | com.github.joostvdg.dui.api.exception.MessageTargetNotAvailableException: Could not send message to 7781
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.propagateMembershipLeaveNotice(DistributedServer.java:125)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.updateMemberShipList(DistributedServer.java:96)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at joostvdg.dui.server@1.0/com.github.joostvdg.dui.server.api.impl.DistributedServer.lambda$listenToInternalCommunication$0(DistributedServer.java:74)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:514)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | 	at java.base/java.lang.Thread.run(Thread.java:844)
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[ERROR]	[21:06:48.004502]	[16]	[Main]				Could not send message to 7781
buming-1_dui.2.lxqo3wd6z1to@dui-5    | [Server-Grace Hopper]			[INFO]	[21:06:48.304198]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=26, type=MEMBERSHIP, message='Hello from Tim Berners-Lee', messageOrigin=MessageOrigin{host='8b058c0af412', ip='10.0.2.114', name='Tim Berners-Lee'}}
buming-1_dui.3.oz7xc37t7l1d@dui-3    | [Server-Alan Kay]				[INFO]	[21:06:48.299228]	[13]	[Main]				 Received multicast: FeiwuMessage{messageSize=26, type=MEMBERSHIP, message='Hello from Tim Berners-Lee', messageOrigin=MessageOrigin{host='8b058c0af412', ip='10.0.2.114', name='Tim Berners-Lee'}}

```
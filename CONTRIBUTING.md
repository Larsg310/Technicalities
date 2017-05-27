# Contributing
## Contribution rules
Most of these rules are of common sense to most, but we're listing them here anyways. They may, however, be updated at any point in time without prior notice.

### In general

In general you **must**:
* Be nice to the other contributors of the repository, be it committers or commenters.
* Explain things clearly. `minceraf crases wen i clic plai` doesn't help anyone.
* Write clean code and use the formatter provided. Someone that hasn't seen it before should be able to understand it.
* Use self-documenting code wherever possible. If you can't do it (and even if you can), comments are always welcome.
* Try to optimize your code before pushing it to the repo. Slow code is bad and you know it! >:(

But you **must NOT**:
* Tag issues. [Suggestion] and [Feature Request] are not needed in the title. The team will assign tags manually.
* Report more than one bug in the same issue, unless they're *very* closely related.
* Open an issue for something that has already been reported.
* "Bump" an issue. We will get to it. Eventually.
* Comment +1 on an issue. Github added reaction buttons for that.
* Try to boss people around if you're not part of the team. We understand that you have an opinion and want everyone to think the same, but that is a bit too much.

---

### Creating or altering code

When altering an already existing file you **must**:
* Keep file and package naming consistent. If you need confirmation, feel free to ask.
* Add documentation to the classes and methods you add, explaining what they do and how they relate to other classes and methods.
* Document the changes you make in the javadoc if they are relevant to the behavior of the class/method.
* Deprecate methods in the API instead of removing them. If they're in an interface, also give them a default body.
* When adding methods to an interface, also give them a method body. Previous implementations will crash otherwise.

But you **must NOT**:
* Include an author tag nor date in the javadoc. We can track changes using git!
* Add mod compatibility outside of a compatibility sub-module.

---

### Creating or altering assets

When adding or modifying an asset you **must**:
* Keep file naming consistent, using `snake_case` and lowercase extensions.
* Always use 100% transparent backgrounds on your assets. Not using them results in rather ugly looking... things.
* Compress the file as much as possible without losing quality.
* Remove file metadata using a program like [PNG Stripper](http://www.steelbytes.com/?mid=30)
* Limit your palette if you can. Gradients look nice in high-res images, but not in 16x16 textures, so pick colors wisely.

But you **must NOT**:
* Use semitransparent pixels on an item texture. Never do this. It's bad and it will haunt you for life.

---

## Contribution workflow

### If you have an idea or recommendation
Open an issue stating your idea in detail, how it should be executed, what its requirements are and what having it would mean for the mod, when in the progression of the mod it should be made available and other relevant information.  
A member of the repo will assign a tag and users will be able to vote for the idea using github's reaction system and give input on it using the comments.  
Once it has been "greenlit", a member of the team will be assigned that task (unless you want to make a pull request for it, in which case you should specify it in the issue body) and it will be given a milestone. That way you can know when the feature is implemented and when you'll be able to get your hands on it. There will be cake to celebrate it, but since we cannot assure it will be able to get to you in one piece, you'll probably just get a virtual hug and a "thank you" :)  
If it's not accepted by either one of the team members (it will be decided amongst everyone, not just the person that closes it) or the community, we will let you know why and how you could improve it. If only a minor change is required, the issue will remain open for a week with the hope that it will be addressed. If it's not changed or the problem isn't minor, it'll be closed, but feel free to open it again once the idea has been polished.

### If you have found a bug
**First check if it has already been reported. If it has, +1 it using github's reaction system. DO NOT OPEN ANOTHER ISSUE.**  
If it hasn't been reported yet, open an issue stating the problem in detail: what you were doing, what should've happened, what actually happened, what version of the mod and Forge you were running, etc... Also provide the crashlog in the form of a pastebin or a gist (other paste services are also alright, but those are the ones we recommend), do **NOT** just copy the crashlog into the issue body.  
A member of the repo will mark it as a bug and will tell you, if possible, how you can fix it temporarily while we figure it out on our end. If it's a mod interaction issue with something like Optifine, chances are we won't be able to fix it, but we'll let you know if that's the case.

### If you want to make a pull request
We recommend first running your pull request through the "idea/recommendation" process to get input on it.  
Try to keep the commits down to the bare minimum and make sure the commit message explains what is done in each one. We recommend also using extended commit messages to explain your changes in more detail. Don't forget to provide a description of what you've done and how it affects the behavior of the mod.  
If your pull request fixes an issue, you can include "Fixes #issueid" in one of the commit messages to automatically close the issue once it's merged. Thank you github overlords for implementing this great feature!  
A member of the repo will comment on it telling you if and how you could improve it. Other people will also be able to help providing feedback on your code, or correcting/suggesting changes to your localizations.  
If it gets pulled, there will be cake to celebrate it, but since we cannot assure it will be able to get to you in one piece, you'll probably just get a virtual hug and a "thank you" :)

---

## If you're not part of the team
Rule n?1 is **do NOT ask to join the team**. If we like what you do, we'll ask you ourselves. If you do ask us it's very likely that nothing special will happen, but you should feel bad for doing it.

~~By contributing to this repository you are effectively selling your body and soul to the Technicalities development team, agreeing to provide support for the thing(s) you contributed with if required without complaints. In exchange you will receive immortality so long as the previous statement remains true.~~

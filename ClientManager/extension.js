// @ts-nocheck
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below

const child = require('child_process');
const vscode = require('vscode');
const fs = require("fs");
const path = require("path");
const rd = require("readline");
var output;
// this method is called when your extension is activated
// your extension is activated the very first time the command is executed

/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {

	console.log('Congratulations, your extension "helloworld" is now active!');

	var className;
	var testClassName;

	let disposableForClass = vscode.commands.registerCommand('extension.loadClassFile', function () {
		var input = vscode.window.showInputBox();

		input.then(function (result) {
			className = result;
			//console.log(typeof(result));
		});
		vscode.window.showInformationMessage("Class Name is : ",className);
	});
	context.subscriptions.push(disposableForClass);

	let disposableForTest = vscode.commands.registerCommand('extension.loadTestFile', function () {
		var input = vscode.window.showInputBox();
		input.then(function (result) {
			testClassName = result;
			//console.log(result);
		});
		vscode.window.showInformationMessage("Class Name is : ",testClassName);
	});
	context.subscriptions.push(disposableForTest);

	let disposable3 = vscode.commands.registerCommand('extension.create', function () {
		//var src="C:\\Users\\Hp\\eclipse-workspace\\SampleProject\\bin";
		var src = vscode.workspace.workspaceFolders[0].uri.path;
		src = src.replace('\/', '');
		src = src.replace(/\//g, "\\\\");
		src = src + "\\\\bin";
		///console.log(src);
		//var dest="C:\\Users\\Hp\\Desktop\\zip\\MyF.zip";
		var dir = __dirname;
		dir = dir.replace(/\\/g, '\\\\');
		var dest = dir + "\\\\source.zip";
		//console.log(dest);
		//var jar="C:\\Users\\Hp\\helloworld\\makeZip.jar";
		var jar = dir + "\\\\makeAndSendZip.jar";
		//className = "triangle.Triangle";
		//testClassName = "triangle.TriangleTest";

		child.exec('java -jar ' + jar + ' ' + src + " " + dest + " " + className + " " + testClassName, function (error, stdout, stderr) {
			//console.log("after execution:");
			//console.log(stdout);
			output=stdout.toString();
			fs.writeFileSync("C:/Users/Hp/helloworld/output.txt", stdout.toString(), { encoding: 'utf8' });
		});
		vscode.window.showInformationMessage("Successfully Generated ");
	});
	context.subscriptions.push(disposable3);

	context.subscriptions.push(
		 vscode.commands.registerCommand('extension.show', function () {
		 const panel = vscode.window.createWebviewPanel(
			'displayer',
			'Score Display With Line Number',
			vscode.ViewColumn.One,
			{}
		);

		// And set its HTML content
		getWebviewContent().then(data => {
			panel.webview.html = data;
		});
		panel.webview.onDidReceiveMessage(
			message=>{
				console.log("reached here");
				switch(message.command){
					case 'goto':
						console.log('1111111111111111111111111111111111 '+message.text);
						return;
				}
			},undefined,context.subscriptions
		);
		 })
		 )
}
exports.activate = activate;

// this method is called when your extension is deactivated
function deactivate() { }

module.exports = {
	activate,
	deactivate
}
function getWebviewContent() {
	return new Promise((resolve, reject) => {
		// const scriptPathOnDisk = vscode.Uri.file(
		// 	path.join(this._extensionPath, 'media', 'main.js')
		// );

		// // And the uri we use to load this script in the webview
		// const scriptUri = webview.asWebviewUri(scriptPathOnDisk);

		// Use a nonce to whitelist which scripts can be run
		//const nonce = getNonce();

		var reader = rd.createInterface(fs.createReadStream("C:/Users/Hp/helloworld/output.txt"));
		var data = [];
		reader.on("line", (l) => {
			var tokens = l.split(' ');
			var nr = tokens[0];
			//var line = tokens[1];
			var score = tokens[1];
			//console.log(`nr: ${l}  to ${score}`);
			data.push({
				number: nr, score
			});
		});
		
		//console.log(`Will be empty data has not yet been read ${data.length}`);

		reader.on("close", () => {
			//console.log(`Data has been read ${data.length}`);
			let tableRows = "";
			data.forEach(element => {
				//console.log(`line number: ${element.number}  score: ${element.score}`);
				tableRows += `<tr>
				<td>${element.number}</td>
				<td>${element.score}</td>
				<td><a href> dop</a>
			  </tr>`;
			});
			let html = getHtml(tableRows);

			resolve(html);
		});

		//console.log(`helosdnbdnabdanm  ${tableRows.length}`);
		let getHtml = (tableRows) => {
			return `<!DOCTYPE html>
			<html lang="en">
			<style>
				
				table, th, td {
					border: 1px solid black;
					border-collapse: collapse;
					background-color: #92a8d1;
					font-weight: bold;
					color:black;
				}
				th, td {
					padding: 15px;
					text-align: left;
					background-color: light-blue;
				}
				table#t01 {
					width: 100%;    
					background-color: #f1f1c1;
				}
				
			</style>
			<head>
				<meta charset="UTF-8">

				<!--
				Use a content security policy to only allow loading images from https or from our extension directory,
				and only allow scripts that have a specific nonce.
				-->
				<meta http-equiv="Content-Security-Policy" content="default-src 'none' ">

				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>Score output</title>
			</head>
			<body>
			  
			  <table style="width:100%">
			  <tr>
				<th>Line Number</th>
				<th>Score</th>
				<th>Link</th>
			  </tr>
			  ${tableRows}
			</table>
			<script>
			(function(){
				const vscode = acquireVsCodeApi();
				
				vscode.postMessage({
					command: 'goto',
					text: 'line'
				})
			}())
			</script>
			</body>
			</html>`;

		};
		
	});
	
}

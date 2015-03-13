package com.nwt.spade.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import javassist.expr.NewArray;

public class MainTest {

	public static void print(Object string) {
		System.out.println(string);
	}

	private String readChoice() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Scanner in = new Scanner(System.in);

		String option = null;

		try {
			// option = br.read();
			option = in.nextLine();
		} catch (Exception ioe) {
			print(ioe.getStackTrace().toString());
			System.exit(1);
		}
		return option;
	}

	@SafeVarargs
	public static <E> ArrayList<E> newArrayList(E... elements) {
		ArrayList<E> list = new ArrayList<>(elements.length);
		Collections.addAll(list, elements);
		return list;
	}

	/*public static void main(String[] args) {
		MainTest test = new MainTest();
		DockerController docker = new DockerController();
		KubernetesController kube = new KubernetesController();
		String fileBase = "/home/badams/dockerfiles";
		// Docker images; 1st index: OS. 2nd index: server type. 3rd index: name
		ArrayList<ArrayList<ArrayList<ArrayList<String>>>> dockerImages = newArrayList(
				newArrayList(
						newArrayList(
								newArrayList("sewatech/modcluster", fileBase
										+ "/ubuntu/apache"),
								newArrayList("bradams/devops:nginx-ubuntu",
										fileBase + "/ubuntu/nginx")),
						newArrayList(
								newArrayList("bradams/devops:wildfly-ubuntu",
										fileBase + "/ubuntu/wildfly"),
								newArrayList("bradams/devops:tomcat-ubuntu",
										fileBase + "/ubuntu/tomcat")),
						newArrayList(
								newArrayList("partlab/ubuntu-mongodb", fileBase
										+ "/ubuntu/mongodb"),
								newArrayList("bradams/devops:mysql-ubuntu",
										fileBase + "/ubuntu/mysql"))),
				newArrayList(
						newArrayList(
								newArrayList("bradams/devops:apache-fedora",
										fileBase + "/fedora/apache"),
								newArrayList("bradams/devops:nginx-fedora", fileBase
										+ "/fedora/nginx")),
						newArrayList(
								newArrayList("bradams/devops:cluster", fileBase
										+ "/fedora/wildfly"),
								newArrayList("bradams/devops:tomcat-fedora",
										fileBase + "/fedora/tomcat")),
						newArrayList(
								newArrayList("bradams/devops:mongodb-fedora",
										fileBase + "/fedora/mongodb"),
								newArrayList("jdeathe/centos-ssh-mysql", fileBase
										+ "/fedora/mysql"))));

		int i = 0;
		while (i < 4) {
			// Choose the OS
			print("Please choose the OS you would like to use:");
			print("1: Ubuntu\n2: Fedora 20");

			int opSys = new Integer(test.readChoice());
			while (opSys<=0 || opSys>2){
				print("Please choose the OS you would like to use:");
				print("1: Ubuntu\n2: Fedora 20");

				opSys = new Integer(test.readChoice());
			}
			print(opSys);
			
			// Choose the server type
			print("Please choose the server you would like to create:");
			print("1: Web server\n2: App server\n3: DB server");
			int server = new Integer(test.readChoice());
			while (server<=0 || server>4){
				print("Please choose the server you would like to create:");
				print("1: Web server\n2: App server\n3: DB server");
			
				server = new Integer(test.readChoice());
			}
			print(server);
			
			// Choose the server type
			print("Please choose the application you would like to use:");
			int app = 0;

			switch (server) {
			case 1:
				print("1: Apache http\n2: Nginx");
				app = new Integer(test.readChoice());
				break;
			case 2:
				print("1: Wildfly\n2: Tomcat");
				app = new Integer(test.readChoice());
				break;
			case 3:
				print("1: MongoDB\n2: MySQL");
				app = new Integer(test.readChoice());
				break;
			}
			while (app<=0 || app>2){
				print("Please choose the application you would like to use:");

				switch (server) {
				case 1:
					print("1: Apache http\n2: Nginx");
					app = new Integer(test.readChoice());
					break;
				case 2:
					print("1: Wildfly\n2: Tomcat");
					app = new Integer(test.readChoice());
					break;
				case 3:
					print("1: MongoDB\n2: MySQL");
					app = new Integer(test.readChoice());
					break;
				}
			}
			print(app);
			String imageName = dockerImages.get(opSys - 1).get(server - 1)
					.get(app - 1).get(0);
			String path = dockerImages.get(opSys - 1).get(server - 1)
					.get(app - 1).get(1);
			//print(imageName);
			//print(path);

			try {
				print("Image used: " + imageName);
				print("Path used: " + path);
				print("Building Docker image with Docker file provided...");
				String imageId = docker.buildImage(imageName, path);

				print("Built and pushed the image with ID " + imageId + ".");

			} catch (Exception ioe) {
				print(ioe.getStackTrace().toString());
				System.exit(1);
			}

			print("Launching the pod at the Kubernetes master.");
			kube.createPod(imageName);

			print("Pod created.");

			i++;

		}

	}*/

}

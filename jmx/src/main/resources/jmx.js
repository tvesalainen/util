/* 
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

"use strict";
/* global source */
var source;
var jmx = {};

jmx.resize = function(){
    var h = $('.windowHeight').height();
    var oh = $('.windowHeight').outerHeight(true);
    var wh = $(window).height();
    $('.windowHeight').height(wh-2*(oh-h));
    };
$(document).ready(function () {
    $('#jstree')
            // listen for event
            .on('changed.jstree', function (e, data) {
                var id = data.node.id;
                if (!id.endsWith('*'))
                {
                    $('#mbean').load('jmx', {'id': id}, function (responseTxt, statusTxt, xhr) {
                        $('.attributeValue').each(function (index, element) {
                            var objectname = $(this).attr('data-objectname');
                            var id = this.id;
                            $(this).load('jmx', {'id': objectname, 'attribute': id}, function (responseTxt, statusTxt, xhr) {
                                $('.attributeInput').change(function () {
                                    var form = $(this).parent();
                                    var data = form.serialize();
                                    $.post('jmx', data), function (responseTxt, statusTxt, xhr) {
                                        a = this;
                                    };
                                })
                            });
                        })
                        $('.operationInvoke').click(function () {
                            var form = $(this).parent();
                            var data = form.serialize();
                            var target = '#'+$(form).find('.operationId').first().attr('value');
                            $(target).load('jmx', data, function (responseTxt, statusTxt, xhr) {
                                a = statusTxt;
                            });
                        })
                        $('.subscribeNotification').click(function () {
                            $("#dialog").dialog("open");
                            var form = $(this).parent();
                            var query = form.serialize();
                            var url = 'jmx?' + query;
                            source = new EventSource(url);
                            source.onmessage = function (event) {
                                $("#dialogTableHeader").after(event.data);
                            };
                        })
                    })
                }
            })
            .jstree({
                'core': {
                    'data': {
                        'url': 'jmx',
                        'data': function (node) {
                            return {'id': node.id};
                        }
                    }
                }
            });
    $("#dialog").dialog({
        autoOpen: false,
        height: 500,
        width: 500,
        buttons: [
            {
                text: "Close",
                click: function () {
                    $(this).dialog("close");
                }
            },
            {
                text: "Pause",
                click: function () {
                    if (source)
                    {
                        source.close();
                    }
                }
            },
        ]
    });
    $("#dialog").on("dialogclose", function (event, ui) {
        if (source)
        {
            source.close();
            $('.notification').remove();
        }
    });
    jmx.resize();
    $("#dialog").dialog('option', 'maxHeight', 0.9*$(window).height());
    $(window).resize(function(){
        jmx.resize();
    });
    
});


